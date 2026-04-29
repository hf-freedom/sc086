import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Table, 
  Button, 
  Modal, 
  Form, 
  Input, 
  Select, 
  InputNumber,
  DatePicker,
  TimePicker,
  message,
  Tag,
  Space,
  Descriptions,
  Divider,
  List
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  EyeOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons';
import { refereeApi, matchApi } from '../api';

const { Option } = Select;

const RefereeList = () => {
  const [loading, setLoading] = useState(false);
  const [referees, setReferees] = useState([]);
  const [matches, setMatches] = useState([]);
  const [selectedReferee, setSelectedReferee] = useState(null);
  
  const [refereeModalVisible, setRefereeModalVisible] = useState(false);
  const [leaveModalVisible, setLeaveModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  
  const [refereeForm] = Form.useForm();
  const [leaveForm] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [refereesRes, matchesRes] = await Promise.all([
        refereeApi.getAll(),
        matchApi.getAll(),
      ]);
      setReferees(refereesRes.data);
      setMatches(matchesRes.data);
    } catch (error) {
      message.error('加载数据失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getSportTypeText = (type) => {
    const map = {
      BASKETBALL: '篮球',
      FOOTBALL: '足球',
      TENNIS: '网球',
      BADMINTON: '羽毛球',
      TABLE_TENNIS: '乒乓球',
      VOLLEYBALL: '排球',
      SWIMMING: '游泳',
      GYMNASTICS: '体操',
    };
    return map[type] || type;
  };

  const handleCreateReferee = () => {
    setSelectedReferee(null);
    refereeForm.resetFields();
    setRefereeModalVisible(true);
  };

  const handleEditReferee = (record) => {
    setSelectedReferee(record);
    refereeForm.setFieldsValue({
      name: record.name,
      phone: record.phone,
      idCard: record.idCard,
      qualifiedSports: record.qualifiedSports,
      qualificationLevel: record.qualificationLevel,
      hourlyRate: record.hourlyRate,
    });
    setRefereeModalVisible(true);
  };

  const handleViewReferee = (record) => {
    setSelectedReferee(record);
    setDetailModalVisible(true);
  };

  const handleSubmitLeave = (record) => {
    setSelectedReferee(record);
    leaveForm.resetFields();
    setLeaveModalVisible(true);
  };

  const handleRefereeSubmit = async () => {
    try {
      const values = await refereeForm.validateFields();
      
      if (selectedReferee) {
        await refereeApi.update(selectedReferee.id, values);
        message.success('更新成功');
      } else {
        await refereeApi.create(values);
        message.success('创建成功');
      }
      
      setRefereeModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleLeaveSubmit = async () => {
    try {
      const values = await leaveForm.validateFields();
      await refereeApi.submitLeave(selectedReferee.id, {
        leaveDate: values.leaveDate.format('YYYY-MM-DD'),
        startTime: values.startTime.format('HH:mm'),
        endTime: values.endTime.format('HH:mm'),
        reason: values.reason,
      });
      message.success('请假申请已提交');
      setLeaveModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleToggleActive = async (id, active) => {
    try {
      await refereeApi.setActive(id, !active);
      message.success('状态更新成功');
      fetchData();
    } catch (error) {
      message.error('更新失败');
      console.error(error);
    }
  };

  const handleApproveLeave = async (refereeId, leaveIndex) => {
    try {
      await refereeApi.approveLeave(refereeId, leaveIndex);
      message.success('已批准请假');
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const sportTypeOptions = [
    { value: 'BASKETBALL', label: '篮球' },
    { value: 'FOOTBALL', label: '足球' },
    { value: 'TENNIS', label: '网球' },
    { value: 'BADMINTON', label: '羽毛球' },
    { value: 'TABLE_TENNIS', label: '乒乓球' },
    { value: 'VOLLEYBALL', label: '排球' },
  ];

  const qualificationOptions = [
    { value: '二级裁判', label: '二级裁判' },
    { value: '一级裁判', label: '一级裁判' },
    { value: '国家级裁判', label: '国家级裁判' },
    { value: '国际级裁判', label: '国际级裁判' },
  ];

  const columns = [
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: '资质项目',
      dataIndex: 'qualifiedSports',
      key: 'qualifiedSports',
      render: (sports) => (
        <span>
          {sports?.map(s => (
            <Tag key={s} color="blue">{getSportTypeText(s)}</Tag>
          ))}
        </span>
      ),
    },
    {
      title: '资质等级',
      dataIndex: 'qualificationLevel',
      key: 'qualificationLevel',
    },
    {
      title: '小时费用',
      dataIndex: 'hourlyRate',
      key: 'hourlyRate',
      render: (rate) => `¥${rate}`,
    },
    {
      title: '状态',
      dataIndex: 'active',
      key: 'active',
      render: (active) => (
        <Tag color={active ? 'green' : 'red'}>
          {active ? '在职' : '离职'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewReferee(record)}>
            详情
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEditReferee(record)}>
            编辑
          </Button>
          <Button type="link" size="small" icon={<CalendarOutlined />} onClick={() => handleSubmitLeave(record)}>
            请假
          </Button>
          <Button 
            type="link" 
            size="small"
            onClick={() => handleToggleActive(record.id, record.active)}
          >
            {record.active ? '禁用' : '启用'}
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div className="page-header">
        <h2>裁判管理</h2>
      </div>

      <Card 
        title="裁判列表" 
        extra={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreateReferee}>
          新建裁判
        </Button>}
      >
        <Table
          dataSource={referees}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>

      <Modal
        title={selectedReferee ? '编辑裁判' : '新建裁判'}
        open={refereeModalVisible}
        onOk={handleRefereeSubmit}
        onCancel={() => setRefereeModalVisible(false)}
      >
        <Form form={refereeForm} layout="vertical">
          <Form.Item
            name="name"
            label="姓名"
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="电话"
            rules={[{ required: true, message: '请输入电话' }]}
          >
            <Input placeholder="请输入电话" />
          </Form.Item>
          <Form.Item
            name="idCard"
            label="身份证号"
          >
            <Input placeholder="请输入身份证号" />
          </Form.Item>
          <Form.Item
            name="qualifiedSports"
            label="资质项目"
            rules={[{ required: true, message: '请选择资质项目' }]}
          >
            <Select mode="multiple" placeholder="请选择资质项目">
              {sportTypeOptions.map(opt => (
                <Option key={opt.value} value={opt.value}>{opt.label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="qualificationLevel"
            label="资质等级"
            rules={[{ required: true, message: '请选择资质等级' }]}
          >
            <Select placeholder="请选择资质等级">
              {qualificationOptions.map(opt => (
                <Option key={opt.value} value={opt.value}>{opt.label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="hourlyRate"
            label="小时费用(元)"
            rules={[{ required: true, message: '请输入小时费用' }]}
          >
            <InputNumber min={0} precision={2} placeholder="请输入小时费用" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="请假申请"
        open={leaveModalVisible}
        onOk={handleLeaveSubmit}
        onCancel={() => setLeaveModalVisible(false)}
      >
        <Form form={leaveForm} layout="vertical">
          <Form.Item
            name="leaveDate"
            label="请假日期"
            rules={[{ required: true, message: '请选择请假日期' }]}
          >
            <DatePicker placeholder="请选择请假日期" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="startTime"
            label="开始时间"
            rules={[{ required: true, message: '请选择开始时间' }]}
          >
            <TimePicker placeholder="请选择开始时间" style={{ width: '100%' }} format="HH:mm" />
          </Form.Item>
          <Form.Item
            name="endTime"
            label="结束时间"
            rules={[{ required: true, message: '请选择结束时间' }]}
          >
            <TimePicker placeholder="请选择结束时间" style={{ width: '100%' }} format="HH:mm" />
          </Form.Item>
          <Form.Item
            name="reason"
            label="请假原因"
            rules={[{ required: true, message: '请输入请假原因' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入请假原因" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="裁判详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedReferee && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="姓名">{selectedReferee.name}</Descriptions.Item>
              <Descriptions.Item label="电话">{selectedReferee.phone}</Descriptions.Item>
              <Descriptions.Item label="资质等级">{selectedReferee.qualificationLevel}</Descriptions.Item>
              <Descriptions.Item label="小时费用">¥{selectedReferee.hourlyRate}</Descriptions.Item>
              <Descriptions.Item label="资质项目" span={2}>
                {selectedReferee.qualifiedSports?.map(s => (
                  <Tag key={s} color="blue">{getSportTypeText(s)}</Tag>
                ))}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={selectedReferee.active ? 'green' : 'red'}>
                  {selectedReferee.active ? '在职' : '离职'}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            <Divider>请假记录</Divider>
            
            {selectedReferee.leaveRecords && selectedReferee.leaveRecords.length > 0 ? (
              <List
                dataSource={selectedReferee.leaveRecords}
                renderItem={(item, index) => (
                  <List.Item
                    actions={[
                      !item.approved && (
                        <Button 
                          type="link" 
                          size="small"
                          icon={<CheckCircleOutlined />}
                          onClick={() => handleApproveLeave(selectedReferee.id, index)}
                        >
                          批准
                        </Button>
                      )
                    ]}
                  >
                    <List.Item.Meta
                      title={`${item.leaveDate} ${item.startTime} - ${item.endTime}`}
                      description={
                        <span>
                          原因: {item.reason}
                          <Tag style={{ marginLeft: 8 }} color={item.approved ? 'green' : 'orange'}>
                            {item.approved ? '已批准' : '待批准'}
                          </Tag>
                        </span>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <div style={{ textAlign: 'center', color: '#999', padding: 20 }}>
                暂无请假记录
              </div>
            )}

            <Divider>已分配赛事</Divider>
            
            {matches.filter(m => m.refereeId === selectedReferee.id).length > 0 ? (
              <List
                dataSource={matches.filter(m => m.refereeId === selectedReferee.id)}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={item.bookingTeamName}
                      description={`${item.matchDate} ${item.startTime} - ${item.endTime} | ${getSportTypeText(item.sportType)}`}
                    />
                  </List.Item>
                )}
              />
            ) : (
              <div style={{ textAlign: 'center', color: '#999', padding: 20 }}>
                暂无已分配赛事
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default RefereeList;