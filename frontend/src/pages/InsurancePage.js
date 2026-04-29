import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Table, 
  Button, 
  Modal, 
  Form, 
  Input, 
  Select,
  message,
  Tag,
  Descriptions,
  Divider,
  Statistic,
  Row,
  Col
} from 'antd';
import { PlusOutlined, EyeOutlined, SafetyOutlined } from '@ant-design/icons';
import { insuranceApi, matchApi } from '../api';

const { Option } = Select;

const InsurancePage = () => {
  const [loading, setLoading] = useState(false);
  const [insurances, setInsurances] = useState([]);
  const [matches, setMatches] = useState([]);
  const [selectedInsurance, setSelectedInsurance] = useState(null);
  const [insuranceInfo, setInsuranceInfo] = useState(null);
  
  const [purchaseModalVisible, setPurchaseModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  
  const [purchaseForm] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [infoRes, matchesRes] = await Promise.all([
        insuranceApi.getInfo(),
        matchApi.getAll(),
      ]);
      setInsuranceInfo(infoRes.data);
      setMatches(matchesRes.data.filter(m => m.status !== 'CANCELLED'));
    } catch (error) {
      message.error('加载数据失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusTag = (active) => (
    <Tag color={active ? 'green' : 'red'}>
      {active ? '有效' : '已失效'}
    </Tag>
  );

  const handleViewInsurance = (record) => {
    setSelectedInsurance(record);
    setDetailModalVisible(true);
  };

  const handlePurchase = () => {
    purchaseForm.resetFields();
    setPurchaseModalVisible(true);
  };

  const handlePurchaseSubmit = async () => {
    try {
      const values = await purchaseForm.validateFields();
      await insuranceApi.purchase(values);
      message.success('购买成功');
      setPurchaseModalVisible(false);
    } catch (error) {
      message.error('购买失败');
      console.error(error);
    }
  };

  const columns = [
    {
      title: '保单号',
      dataIndex: 'policyNumber',
      key: 'policyNumber',
    },
    {
      title: '参保人员',
      dataIndex: 'participantName',
      key: 'participantName',
    },
    {
      title: '联系电话',
      dataIndex: 'participantPhone',
      key: 'participantPhone',
    },
    {
      title: '保费(元)',
      dataIndex: 'premium',
      key: 'premium',
      render: (p) => `¥${p}`,
    },
    {
      title: '保额(元)',
      dataIndex: 'coverageAmount',
      key: 'coverageAmount',
      render: (c) => `¥${c}`,
    },
    {
      title: '保险期间',
      key: 'period',
      render: (_, record) => `${record.startDate} 至 ${record.endDate}`,
    },
    {
      title: '状态',
      dataIndex: 'active',
      key: 'active',
      render: (active) => getStatusTag(active),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewInsurance(record)}>
          详情
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div className="page-header">
        <h2>保险管理</h2>
      </div>

      {insuranceInfo && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col span={8}>
            <Card>
              <Statistic
                title="标准保费"
                value={insuranceInfo.standardPremium}
                prefix="¥"
                suffix="/份"
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic
                title="保障金额"
                value={insuranceInfo.standardCoverage}
                prefix="¥"
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic
                title="保险类型"
                value="体育赛事意外险"
                prefix={<SafetyOutlined />}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card 
        title="保单列表"
        extra={<Button type="primary" icon={<PlusOutlined />} onClick={handlePurchase}>
          购买保险
        </Button>}
      >
        <Table
          dataSource={insurances}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Card title="保险说明" style={{ marginTop: 24 }}>
        <h4>购买须知：</h4>
        <ul>
          <li>每份保险保费 50 元，保障金额 100,000 元</li>
          <li>保险期间为赛事当日及次日</li>
          <li>保障范围：体育赛事过程中发生的意外伤害</li>
          <li>购买后即时生效，保单号自动生成</li>
        </ul>
        
        <h4 style={{ marginTop: 16 }}>退款政策：</h4>
        <ul>
          <li>赛事因天气原因取消：可申请全额退保</li>
          <li>赛事因场馆维护取消：可申请全额退保</li>
          <li>团队主动取消：根据赛事取消退款政策处理</li>
        </ul>
      </Card>

      <Modal
        title="购买保险"
        open={purchaseModalVisible}
        onOk={handlePurchaseSubmit}
        onCancel={() => setPurchaseModalVisible(false)}
      >
        <Form form={purchaseForm} layout="vertical">
          <Form.Item
            name="matchId"
            label="选择赛事"
            rules={[{ required: true, message: '请选择赛事' }]}
          >
            <Select placeholder="请选择赛事">
              {matches.map(m => (
                <Option key={m.id} value={m.id}>
                  {m.bookingTeamName} - {m.matchDate} {m.startTime}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="participantName"
            label="参保人员姓名"
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item
            name="participantIdCard"
            label="身份证号"
            rules={[{ required: true, message: '请输入身份证号' }]}
          >
            <Input placeholder="请输入身份证号" />
          </Form.Item>
          <Form.Item
            name="participantPhone"
            label="联系电话"
            rules={[{ required: true, message: '请输入联系电话' }]}
          >
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          
          <div style={{ background: '#f6ffed', border: '1px solid #b7eb8f', padding: 16, borderRadius: 8 }}>
            <p><strong>保费：</strong>¥50.00 / 份</p>
            <p><strong>保额：</strong>¥100,000.00</p>
            <p><strong>保险类型：</strong>体育赛事意外险</p>
          </div>
        </Form>
      </Modal>

      <Modal
        title="保单详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
      >
        {selectedInsurance && (
          <div>
            <Descriptions bordered column={1}>
              <Descriptions.Item label="保单号">{selectedInsurance.policyNumber}</Descriptions.Item>
              <Descriptions.Item label="参保人员">
                {selectedInsurance.participantName}
              </Descriptions.Item>
              <Descriptions.Item label="身份证号">
                {selectedInsurance.participantIdCard}
              </Descriptions.Item>
              <Descriptions.Item label="联系电话">
                {selectedInsurance.participantPhone}
              </Descriptions.Item>
              <Descriptions.Item label="保费">¥{selectedInsurance.premium}</Descriptions.Item>
              <Descriptions.Item label="保额">¥{selectedInsurance.coverageAmount}</Descriptions.Item>
              <Descriptions.Item label="保险类型">{selectedInsurance.insuranceType}</Descriptions.Item>
              <Descriptions.Item label="保险期间">
                {selectedInsurance.startDate} 至 {selectedInsurance.endDate}
              </Descriptions.Item>
              <Descriptions.Item label="购买时间">
                {selectedInsurance.purchasedAt}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {getStatusTag(selectedInsurance.active)}
              </Descriptions.Item>
            </Descriptions>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default InsurancePage;