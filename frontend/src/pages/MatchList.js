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
  Popconfirm,
  Tag,
  Divider,
  Descriptions,
  Space,
  Collapse
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  EyeOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  StopOutlined,
  SyncOutlined
} from '@ant-design/icons';
import { matchApi, stadiumApi, courtApi, refereeApi, cancellationApi } from '../api';
import dayjs from 'dayjs';

const { Option } = Select;
const { Panel } = Collapse;

const MatchList = () => {
  const [loading, setLoading] = useState(false);
  const [matches, setMatches] = useState([]);
  const [stadiums, setStadiums] = useState([]);
  const [courts, setCourts] = useState([]);
  const [referees, setReferees] = useState([]);
  const [selectedMatch, setSelectedMatch] = useState(null);
  
  const [matchModalVisible, setMatchModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [cancelModalVisible, setCancelModalVisible] = useState(false);
  const [rescheduleModalVisible, setRescheduleModalVisible] = useState(false);
  
  const [matchForm] = Form.useForm();
  const [cancelForm] = Form.useForm();
  const [rescheduleForm] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [matchesRes, stadiumsRes, courtsRes, refereesRes] = await Promise.all([
        matchApi.getAll(),
        stadiumApi.getAll(),
        courtApi.getAll(),
        refereeApi.getAll(),
      ]);
      setMatches(matchesRes.data);
      setStadiums(stadiumsRes.data);
      setCourts(courtsRes.data);
      setReferees(refereesRes.data);
    } catch (error) {
      message.error('加载数据失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusTag = (status) => {
    const statusMap = {
      BOOKED: { color: 'blue', text: '已预约' },
      PENDING_CONFIRM: { color: 'orange', text: '待确认' },
      CONFIRMED: { color: 'green', text: '已确认' },
      IN_PROGRESS: { color: 'cyan', text: '进行中' },
      COMPLETED: { color: 'purple', text: '已完成' },
      CANCELLED: { color: 'red', text: '已取消' },
      RESCHEDULED: { color: 'gold', text: '已改期' },
    };
    const info = statusMap[status] || { color: 'default', text: status };
    return <Tag color={info.color}>{info.text}</Tag>;
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

  const handleCreateMatch = () => {
    matchForm.resetFields();
    matchForm.setFieldsValue({
      durationMinutes: 120,
      isOfficialMatch: false,
    });
    setMatchModalVisible(true);
  };

  const handleViewMatch = (record) => {
    setSelectedMatch(record);
    setDetailModalVisible(true);
  };

  const handleStartMatch = async (id) => {
    try {
      await matchApi.start(id);
      message.success('赛事已开始');
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleConfirmMatch = async (id) => {
    try {
      await matchApi.confirm(id);
      message.success('赛事已确认');
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleReassignReferee = async (id) => {
    try {
      const res = await matchApi.reassignReferee(id);
      if (res.data.status === 'CONFIRMED') {
        message.success('重新分配裁判成功');
      } else {
        message.warning('暂无可用裁判，请手动确认');
      }
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleCompleteMatch = async (id) => {
    try {
      await matchApi.complete(id, {});
      message.success('赛事已完成');
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleCancelMatch = (record) => {
    setSelectedMatch(record);
    cancelForm.resetFields();
    setCancelModalVisible(true);
  };

  const handleRescheduleMatch = (record) => {
    setSelectedMatch(record);
    rescheduleForm.resetFields();
    setRescheduleModalVisible(true);
  };

  const handleMatchSubmit = async () => {
    try {
      const values = await matchForm.validateFields();
      
      const params = {
        bookingTeamName: values.bookingTeamName,
        contactPerson: values.contactPerson,
        contactPhone: values.contactPhone,
        sportType: values.sportType,
        participantCount: values.participantCount,
        stadiumId: values.stadiumId,
        courtId: values.courtId,
        matchDate: values.matchDate.format('YYYY-MM-DD'),
        startTime: values.startTime.format('HH:mm'),
        durationMinutes: values.durationMinutes,
        isOfficialMatch: values.isOfficialMatch,
      };
      
      await matchApi.create(params);
      message.success('创建成功');
      setMatchModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('创建失败: ' + (error.response?.data?.message || error.message));
      console.error(error);
    }
  };

  const handleCancelSubmit = async () => {
    try {
      const values = await cancelForm.validateFields();
      await cancellationApi.cancel(selectedMatch.id, {
        reason: values.reason,
        note: values.note,
      });
      message.success('取消成功');
      setCancelModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleRescheduleSubmit = async () => {
    try {
      const values = await rescheduleForm.validateFields();
      await cancellationApi.reschedule(selectedMatch.id, {
        newDate: values.newDate.format('YYYY-MM-DD'),
        newStartTime: values.newStartTime.format('HH:mm'),
        newDurationMinutes: values.newDurationMinutes,
      });
      message.success('改期成功');
      setRescheduleModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const columns = [
    {
      title: '预约队伍',
      dataIndex: 'bookingTeamName',
      key: 'bookingTeamName',
    },
    {
      title: '项目类型',
      dataIndex: 'sportType',
      key: 'sportType',
      render: (type) => getSportTypeText(type),
    },
    {
      title: '比赛日期',
      dataIndex: 'matchDate',
      key: 'matchDate',
    },
    {
      title: '时间',
      key: 'time',
      render: (_, record) => `${record.startTime} - ${record.endTime}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewMatch(record)}>
            详情
          </Button>
          {record.status === 'PENDING_CONFIRM' && (
            <>
              <Button type="link" size="small" icon={<CheckCircleOutlined />} onClick={() => handleConfirmMatch(record.id)}>
                确认
              </Button>
              <Button type="link" size="small" icon={<SyncOutlined />} onClick={() => handleReassignReferee(record.id)}>
                重派裁判
              </Button>
            </>
          )}
          {record.status === 'CONFIRMED' && (
            <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={() => handleStartMatch(record.id)}>
              开始
            </Button>
          )}
          {record.status === 'IN_PROGRESS' && (
            <Button type="link" size="small" icon={<CheckCircleOutlined />} onClick={() => handleCompleteMatch(record.id)}>
              完成
            </Button>
          )}
          {['BOOKED', 'PENDING_CONFIRM', 'CONFIRMED', 'RESCHEDULED'].includes(record.status) && (
            <>
              <Button type="link" size="small" icon={<SyncOutlined />} onClick={() => handleRescheduleMatch(record)}>
                改期
              </Button>
              <Button type="link" size="small" danger icon={<StopOutlined />} onClick={() => handleCancelMatch(record)}>
                取消
              </Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  const sportTypeOptions = [
    { value: 'BASKETBALL', label: '篮球' },
    { value: 'FOOTBALL', label: '足球' },
    { value: 'TENNIS', label: '网球' },
    { value: 'BADMINTON', label: '羽毛球' },
    { value: 'TABLE_TENNIS', label: '乒乓球' },
    { value: 'VOLLEYBALL', label: '排球' },
  ];

  const cancellationReasonOptions = [
    { value: 'WEATHER', label: '天气原因' },
    { value: 'TEAM_CANCEL', label: '团队取消' },
    { value: 'REFEREE_LEAVE', label: '裁判请假' },
    { value: 'VENUE_MAINTENANCE', label: '场馆维护' },
    { value: 'OTHER', label: '其他原因' },
  ];

  return (
    <div>
      <div className="page-header">
        <h2>赛事预约</h2>
      </div>

      <Card 
        title="赛事列表" 
        extra={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreateMatch}>
          新建预约
        </Button>}
      >
        <Table
          dataSource={matches}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="新建赛事预约"
        open={matchModalVisible}
        onOk={handleMatchSubmit}
        onCancel={() => setMatchModalVisible(false)}
        width={700}
      >
        <Form form={matchForm} layout="vertical">
          <Collapse defaultActiveKey={['basic', 'booking']}>
            <Panel header="基本信息" key="basic">
              <Form.Item
                name="bookingTeamName"
                label="预约队伍名称"
                rules={[{ required: true, message: '请输入队伍名称' }]}
              >
                <Input placeholder="请输入队伍名称" />
              </Form.Item>
              <Form.Item
                name="sportType"
                label="运动项目"
                rules={[{ required: true, message: '请选择运动项目' }]}
              >
                <Select placeholder="请选择运动项目">
                  {sportTypeOptions.map(opt => (
                    <Option key={opt.value} value={opt.value}>{opt.label}</Option>
                  ))}
                </Select>
              </Form.Item>
              <Form.Item
                name="participantCount"
                label="参与人数"
                rules={[{ required: true, message: '请输入参与人数' }]}
              >
                <InputNumber min={1} placeholder="请输入参与人数" style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item
                name="isOfficialMatch"
                label="是否正式赛事"
                valuePropName="checked"
              >
                <Select>
                  <Option value={true}>是(需要裁判)</Option>
                  <Option value={false}>否(友谊赛)</Option>
                </Select>
              </Form.Item>
            </Panel>
            
            <Panel header="联系人信息" key="contact">
              <Form.Item
                name="contactPerson"
                label="联系人"
                rules={[{ required: true, message: '请输入联系人' }]}
              >
                <Input placeholder="请输入联系人" />
              </Form.Item>
              <Form.Item
                name="contactPhone"
                label="联系电话"
                rules={[{ required: true, message: '请输入联系电话' }]}
              >
                <Input placeholder="请输入联系电话" />
              </Form.Item>
            </Panel>

            <Panel header="场地和时间" key="booking">
              <Form.Item
                name="stadiumId"
                label="选择场馆"
                rules={[{ required: true, message: '请选择场馆' }]}
              >
                <Select placeholder="请选择场馆">
                  {stadiums.map(s => (
                    <Option key={s.id} value={s.id}>{s.name}</Option>
                  ))}
                </Select>
              </Form.Item>
              <Form.Item
                name="courtId"
                label="选择场地"
                rules={[{ required: true, message: '请选择场地' }]}
              >
                <Select placeholder="请选择场地" showSearch optionFilterProp="children">
                  {courts.map(c => {
                    const stadium = stadiums.find(s => s.id === c.stadiumId);
                    return (
                      <Option key={c.id} value={c.id}>
                        [{stadium?.name || '未知场馆'}] - {c.name} - ¥{c.hourlyRate}/小时
                      </Option>
                    );
                  })}
                </Select>
              </Form.Item>
              <Form.Item
                name="matchDate"
                label="比赛日期"
                rules={[{ required: true, message: '请选择比赛日期' }]}
              >
                <DatePicker 
                  placeholder="请选择比赛日期" 
                  style={{ width: '100%' }}
                  disabledDate={(current) => current && current < dayjs().startOf('day')}
                />
              </Form.Item>
              <Form.Item
                name="startTime"
                label="开始时间"
                rules={[{ required: true, message: '请选择开始时间' }]}
              >
                <TimePicker 
                  placeholder="请选择开始时间" 
                  style={{ width: '100%' }}
                  format="HH:mm"
                  minuteStep={30}
                />
              </Form.Item>
              <Form.Item
                name="durationMinutes"
                label="预计时长(分钟)"
                rules={[{ required: true, message: '请输入预计时长' }]}
              >
                <Select>
                  <Option value={60}>60分钟(1小时)</Option>
                  <Option value={90}>90分钟(1.5小时)</Option>
                  <Option value={120}>120分钟(2小时)</Option>
                  <Option value={180}>180分钟(3小时)</Option>
                </Select>
              </Form.Item>
            </Panel>
          </Collapse>
        </Form>
      </Modal>

      <Modal
        title="赛事详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedMatch && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="预约队伍">{selectedMatch.bookingTeamName}</Descriptions.Item>
              <Descriptions.Item label="项目类型">{getSportTypeText(selectedMatch.sportType)}</Descriptions.Item>
              <Descriptions.Item label="联系人">{selectedMatch.contactPerson}</Descriptions.Item>
              <Descriptions.Item label="联系电话">{selectedMatch.contactPhone}</Descriptions.Item>
              <Descriptions.Item label="比赛日期">{selectedMatch.matchDate}</Descriptions.Item>
              <Descriptions.Item label="时间">{selectedMatch.startTime} - {selectedMatch.endTime}</Descriptions.Item>
              <Descriptions.Item label="参与人数">{selectedMatch.participantCount}人</Descriptions.Item>
              <Descriptions.Item label="赛事类型">{selectedMatch.isOfficialMatch ? '正式赛事' : '友谊赛'}</Descriptions.Item>
              <Descriptions.Item label="状态" span={2}>{getStatusTag(selectedMatch.status)}</Descriptions.Item>
            </Descriptions>
            
            <Divider>费用信息</Divider>
            
            <Descriptions bordered column={2}>
              <Descriptions.Item label="场地费用">¥{selectedMatch.venueFee}</Descriptions.Item>
              <Descriptions.Item label="裁判费用">¥{selectedMatch.refereeFee}</Descriptions.Item>
              <Descriptions.Item label="超时费用">¥{selectedMatch.overtimeFee}</Descriptions.Item>
              <Descriptions.Item label="保险费用">¥{selectedMatch.insuranceFee}</Descriptions.Item>
              <Descriptions.Item label="总金额" span={2}>
                <span style={{ color: '#1890ff', fontWeight: 'bold', fontSize: 16 }}>
                  ¥{selectedMatch.totalAmount}
                </span>
              </Descriptions.Item>
              {selectedMatch.status === 'CANCELLED' && (
                <>
                  <Descriptions.Item label="取消原因" span={2}>{selectedMatch.cancellationReason}</Descriptions.Item>
                  <Descriptions.Item label="退款金额" span={2}>
                    <span style={{ color: '#52c41a', fontWeight: 'bold' }}>
                      ¥{selectedMatch.refundAmount}
                    </span>
                  </Descriptions.Item>
                </>
              )}
            </Descriptions>
          </div>
        )}
      </Modal>

      <Modal
        title="取消赛事"
        open={cancelModalVisible}
        onOk={handleCancelSubmit}
        onCancel={() => setCancelModalVisible(false)}
      >
        <Form form={cancelForm} layout="vertical">
          <Form.Item
            name="reason"
            label="取消原因"
            rules={[{ required: true, message: '请选择取消原因' }]}
          >
            <Select placeholder="请选择取消原因">
              {cancellationReasonOptions.map(opt => (
                <Option key={opt.value} value={opt.value}>{opt.label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="note"
            label="备注说明"
          >
            <Input.TextArea rows={3} placeholder="请输入备注说明" />
          </Form.Item>
          <div style={{ background: '#f5f5f5', padding: 16, borderRadius: 8 }}>
            <h4>退款政策：</h4>
            <ul>
              <li>天气原因、场馆维护、裁判请假 - 全额退款</li>
              <li>团队取消：
                <ul>
                  <li>提前48小时 - 全额退款</li>
                  <li>提前24-48小时 - 80%退款</li>
                  <li>提前12-24小时 - 50%退款</li>
                  <li>提前少于12小时 - 不退款</li>
                </ul>
              </li>
            </ul>
          </div>
        </Form>
      </Modal>

      <Modal
        title="赛事改期"
        open={rescheduleModalVisible}
        onOk={handleRescheduleSubmit}
        onCancel={() => setRescheduleModalVisible(false)}
      >
        <Form form={rescheduleForm} layout="vertical">
          <Form.Item
            name="newDate"
            label="新的比赛日期"
            rules={[{ required: true, message: '请选择比赛日期' }]}
          >
            <DatePicker 
              placeholder="请选择比赛日期" 
              style={{ width: '100%' }}
              disabledDate={(current) => current && current < dayjs().startOf('day')}
            />
          </Form.Item>
          <Form.Item
            name="newStartTime"
            label="新的开始时间"
            rules={[{ required: true, message: '请选择开始时间' }]}
          >
            <TimePicker 
              placeholder="请选择开始时间" 
              style={{ width: '100%' }}
              format="HH:mm"
              minuteStep={30}
            />
          </Form.Item>
          <Form.Item
            name="newDurationMinutes"
            label="预计时长(分钟)"
            rules={[{ required: true, message: '请输入预计时长' }]}
          >
            <Select>
              <Option value={60}>60分钟(1小时)</Option>
              <Option value={90}>90分钟(1.5小时)</Option>
              <Option value={120}>120分钟(2小时)</Option>
              <Option value={180}>180分钟(3小时)</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MatchList;