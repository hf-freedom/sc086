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
  message,
  Popconfirm,
  Tag,
  Divider,
  List,
  Descriptions
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { stadiumApi, courtApi } from '../api';

const { Option } = Select;

const StadiumList = () => {
  const [loading, setLoading] = useState(false);
  const [stadiums, setStadiums] = useState([]);
  const [courts, setCourts] = useState([]);
  const [selectedStadium, setSelectedStadium] = useState(null);
  
  const [stadiumModalVisible, setStadiumModalVisible] = useState(false);
  const [courtModalVisible, setCourtModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [editingStadium, setEditingStadium] = useState(null);
  const [editingCourt, setEditingCourt] = useState(null);
  
  const [stadiumForm] = Form.useForm();
  const [courtForm] = Form.useForm();

  useEffect(() => {
    fetchStadiums();
    fetchCourts();
  }, []);

  const fetchStadiums = async () => {
    try {
      setLoading(true);
      const res = await stadiumApi.getAll();
      setStadiums(res.data);
    } catch (error) {
      message.error('加载场馆列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCourts = async () => {
    try {
      const res = await courtApi.getAll();
      setCourts(res.data);
    } catch (error) {
      message.error('加载场地列表失败');
      console.error(error);
    }
  };

  const handleCreateStadium = () => {
    setEditingStadium(null);
    stadiumForm.resetFields();
    setStadiumModalVisible(true);
  };

  const handleEditStadium = (record) => {
    setEditingStadium(record);
    stadiumForm.setFieldsValue({
      name: record.name,
      address: record.address,
      venueType: record.venueType,
    });
    setStadiumModalVisible(true);
  };

  const handleDeleteStadium = async (id) => {
    try {
      await stadiumApi.delete(id);
      message.success('删除成功');
      fetchStadiums();
    } catch (error) {
      message.error('删除失败');
      console.error(error);
    }
  };

  const handleViewStadium = (record) => {
    setSelectedStadium(record);
    setDetailModalVisible(true);
  };

  const handleStadiumSubmit = async () => {
    try {
      const values = await stadiumForm.validateFields();
      
      if (editingStadium) {
        await stadiumApi.update(editingStadium.id, values);
        message.success('更新成功');
      } else {
        await stadiumApi.create(values);
        message.success('创建成功');
      }
      
      setStadiumModalVisible(false);
      fetchStadiums();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleCreateCourt = (stadiumId) => {
    setSelectedStadium(stadiums.find(s => s.id === stadiumId));
    setEditingCourt(null);
    courtForm.resetFields();
    setCourtModalVisible(true);
  };

  const handleCourtSubmit = async () => {
    try {
      const values = await courtForm.validateFields();
      
      if (editingCourt) {
        await courtApi.update(editingCourt.id, values);
        message.success('更新成功');
      } else if (selectedStadium) {
        await stadiumApi.addCourt(selectedStadium.id, values);
        message.success('创建成功');
      }
      
      setCourtModalVisible(false);
      fetchCourts();
    } catch (error) {
      message.error('操作失败');
      console.error(error);
    }
  };

  const handleToggleCourtActive = async (id, active) => {
    try {
      await courtApi.setActive(id, !active);
      message.success('状态更新成功');
      fetchCourts();
    } catch (error) {
      message.error('更新失败');
      console.error(error);
    }
  };

  const venueTypeOptions = [
    { value: 'INDOOR', label: '室内' },
    { value: 'OUTDOOR', label: '室外' },
    { value: 'BOTH', label: '室内外均可' },
  ];

  const sportTypeOptions = [
    { value: 'BASKETBALL', label: '篮球' },
    { value: 'FOOTBALL', label: '足球' },
    { value: 'TENNIS', label: '网球' },
    { value: 'BADMINTON', label: '羽毛球' },
    { value: 'TABLE_TENNIS', label: '乒乓球' },
    { value: 'VOLLEYBALL', label: '排球' },
    { value: 'SWIMMING', label: '游泳' },
    { value: 'GYMNASTICS', label: '体操' },
  ];

  const stadiumColumns = [
    {
      title: '场馆名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '地址',
      dataIndex: 'address',
      key: 'address',
    },
    {
      title: '场馆类型',
      dataIndex: 'venueType',
      key: 'venueType',
      render: (type) => {
        const map = {
          INDOOR: '室内',
          OUTDOOR: '室外',
          BOTH: '室内外均可',
        };
        return <Tag>{map[type] || type}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <div className="table-actions">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewStadium(record)}>
            查看
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEditStadium(record)}>
            编辑
          </Button>
          <Button type="link" size="small" icon={<PlusOutlined />} onClick={() => handleCreateCourt(record.id)}>
            添加场地
          </Button>
          <Popconfirm title="确定删除?" onConfirm={() => handleDeleteStadium(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </div>
      ),
    },
  ];

  const courtColumns = [
    {
      title: '场地名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '场地编号',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: '支持项目',
      dataIndex: 'supportedSports',
      key: 'supportedSports',
      render: (sports) => (
        <span>
          {sports?.map(s => {
            const sportMap = {
              BASKETBALL: '篮球',
              FOOTBALL: '足球',
              TENNIS: '网球',
              BADMINTON: '羽毛球',
              TABLE_TENNIS: '乒乓球',
              VOLLEYBALL: '排球',
              SWIMMING: '游泳',
              GYMNASTICS: '体操',
            };
            return <Tag key={s}>{sportMap[s] || s}</Tag>;
          })}
        </span>
      ),
    },
    {
      title: '小时单价',
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
          {active ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button 
          type="link" 
          size="small"
          onClick={() => handleToggleCourtActive(record.id, record.active)}
        >
          {record.active ? '禁用' : '启用'}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div className="page-header">
        <h2>场馆管理</h2>
      </div>

      <Card title="场馆列表" extra={<Button type="primary" icon={<PlusOutlined />} onClick={handleCreateStadium}>
        新建场馆
      </Button>}>
        <Table
          dataSource={stadiums}
          columns={stadiumColumns}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>

      <Divider />

      <Card title="场地列表">
        <Table
          dataSource={courts}
          columns={courtColumns}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Modal
        title={editingStadium ? '编辑场馆' : '新建场馆'}
        open={stadiumModalVisible}
        onOk={handleStadiumSubmit}
        onCancel={() => setStadiumModalVisible(false)}
      >
        <Form form={stadiumForm} layout="vertical">
          <Form.Item
            name="name"
            label="场馆名称"
            rules={[{ required: true, message: '请输入场馆名称' }]}
          >
            <Input placeholder="请输入场馆名称" />
          </Form.Item>
          <Form.Item
            name="address"
            label="地址"
            rules={[{ required: true, message: '请输入地址' }]}
          >
            <Input placeholder="请输入地址" />
          </Form.Item>
          <Form.Item
            name="venueType"
            label="场馆类型"
            rules={[{ required: true, message: '请选择场馆类型' }]}
          >
            <Select placeholder="请选择场馆类型">
              {venueTypeOptions.map(opt => (
                <Option key={opt.value} value={opt.value}>{opt.label}</Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={editingCourt ? '编辑场地' : '新建场地'}
        open={courtModalVisible}
        onOk={handleCourtSubmit}
        onCancel={() => setCourtModalVisible(false)}
      >
        <Form form={courtForm} layout="vertical">
          <Form.Item
            name="name"
            label="场地名称"
            rules={[{ required: true, message: '请输入场地名称' }]}
          >
            <Input placeholder="请输入场地名称" />
          </Form.Item>
          <Form.Item
            name="code"
            label="场地编号"
            rules={[{ required: true, message: '请输入场地编号' }]}
          >
            <Input placeholder="例如: B-001" />
          </Form.Item>
          <Form.Item
            name="supportedSports"
            label="支持项目"
            rules={[{ required: true, message: '请选择支持的项目' }]}
          >
            <Select mode="multiple" placeholder="请选择支持的项目">
              {sportTypeOptions.map(opt => (
                <Option key={opt.value} value={opt.value}>{opt.label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="capacity"
            label="容量"
            rules={[{ required: true, message: '请输入容量' }]}
          >
            <InputNumber min={1} placeholder="请输入容量" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="hourlyRate"
            label="小时单价(元)"
            rules={[{ required: true, message: '请输入小时单价' }]}
          >
            <InputNumber min={0} precision={2} placeholder="请输入小时单价" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="overtimeRate"
            label="超时单价(元)"
          >
            <InputNumber min={0} precision={2} placeholder="超时单价(默认同小时单价)" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="场馆详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedStadium && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="场馆名称">{selectedStadium.name}</Descriptions.Item>
              <Descriptions.Item label="场馆类型">
                {venueTypeOptions.find(o => o.value === selectedStadium.venueType)?.label || selectedStadium.venueType}
              </Descriptions.Item>
              <Descriptions.Item label="地址" span={2}>{selectedStadium.address}</Descriptions.Item>
              <Descriptions.Item label="支持项目" span={2}>
                {selectedStadium.supportedSports?.map(s => {
                  const sportMap = {
                    BASKETBALL: '篮球',
                    FOOTBALL: '足球',
                    TENNIS: '网球',
                    BADMINTON: '羽毛球',
                    TABLE_TENNIS: '乒乓球',
                    VOLLEYBALL: '排球',
                    SWIMMING: '游泳',
                    GYMNASTICS: '体操',
                  };
                  return <Tag key={s}>{sportMap[s] || s}</Tag>;
                })}
              </Descriptions.Item>
            </Descriptions>
            
            <Divider>场地列表</Divider>
            
            <List
              dataSource={courts.filter(c => c.stadiumId === selectedStadium.id)}
              renderItem={item => (
                <List.Item>
                  <List.Item.Meta
                    title={item.name}
                    description={`编号: ${item.code} | 单价: ¥${item.hourlyRate}/小时 | 状态: ${item.active ? '启用' : '禁用'}`}
                  />
                </List.Item>
              )}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};

export default StadiumList;