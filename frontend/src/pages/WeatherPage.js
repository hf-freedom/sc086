import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Button, 
  Modal, 
  Form, 
  Input, 
  Select,
  InputNumber,
  message,
  Tag,
  Row,
  Col,
  Statistic,
  Descriptions,
  Alert,
  Divider
} from 'antd';
import { 
  CloudOutlined, 
  SunOutlined, 
  ThunderboltOutlined,
  PlusOutlined,
  WarningOutlined
} from '@ant-design/icons';
import { weatherApi } from '../api';
import dayjs from 'dayjs';

const { Option } = Select;

const WeatherPage = () => {
  const [loading, setLoading] = useState(false);
  const [currentWeather, setCurrentWeather] = useState(null);
  const [weatherHistory, setWeatherHistory] = useState([]);
  
  const [recordModalVisible, setRecordModalVisible] = useState(false);
  const [recordForm] = Form.useForm();

  useEffect(() => {
    fetchCurrentWeather();
  }, []);

  const fetchCurrentWeather = async () => {
    try {
      setLoading(true);
      const res = await weatherApi.getCurrent(dayjs().format('YYYY-MM-DD'));
      setCurrentWeather(res.data);
    } catch (error) {
      message.error('加载天气数据失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getWeatherIcon = (condition) => {
    const iconMap = {
      SUNNY: <SunOutlined style={{ fontSize: 48, color: '#faad14' }} />,
      CLOUDY: <CloudOutlined style={{ fontSize: 48, color: '#8c8c8c' }} />,
      RAINY: <CloudOutlined style={{ fontSize: 48, color: '#1890ff' }} />,
      STORMY: <ThunderboltOutlined style={{ fontSize: 48, color: '#cf1322' }} />,
      SNOWY: <CloudOutlined style={{ fontSize: 48, color: '#d9d9d9' }} />,
      FOGGY: <CloudOutlined style={{ fontSize: 48, color: '#bfbfbf' }} />,
      EXTREME_HEAT: <SunOutlined style={{ fontSize: 48, color: '#ff4d4f' }} />,
      EXTREME_COLD: <CloudOutlined style={{ fontSize: 48, color: '#40a9ff' }} />,
    };
    return iconMap[condition] || <CloudOutlined style={{ fontSize: 48 }} />;
  };

  const getConditionText = (condition) => {
    const textMap = {
      SUNNY: '晴天',
      CLOUDY: '多云',
      RAINY: '下雨',
      STORMY: '暴风雨',
      SNOWY: '下雪',
      FOGGY: '大雾',
      EXTREME_HEAT: '极端高温',
      EXTREME_COLD: '极端低温',
    };
    return textMap[condition] || condition;
  };

  const handleRecordSubmit = async () => {
    try {
      const values = await recordForm.validateFields();
      await weatherApi.record({
        date: values.date?.format('YYYY-MM-DD') || dayjs().format('YYYY-MM-DD'),
        condition: values.condition,
        description: values.description,
        temperature: values.temperature,
        humidity: values.humidity,
        windDirection: values.windDirection,
        windSpeed: values.windSpeed,
      });
      message.success('天气记录已录入');
      setRecordModalVisible(false);
      fetchCurrentWeather();
    } catch (error) {
      message.error('录入失败');
      console.error(error);
    }
  };

  const weatherConditionOptions = [
    { value: 'SUNNY', label: '晴天' },
    { value: 'CLOUDY', label: '多云' },
    { value: 'RAINY', label: '下雨' },
    { value: 'STORMY', label: '暴风雨' },
    { value: 'SNOWY', label: '下雪' },
    { value: 'FOGGY', label: '大雾' },
    { value: 'EXTREME_HEAT', label: '极端高温' },
    { value: 'EXTREME_COLD', label: '极端低温' },
  ];

  return (
    <div>
      <div className="page-header">
        <h2>天气监控</h2>
      </div>

      {currentWeather && currentWeather.abnormal && (
        <Alert
          message="天气异常提醒"
          description={
            <span>
              当前天气: {currentWeather.description}。室外赛事可能受到影响，系统将自动检查并取消受影响的室外赛事。
            </span>
          }
          type="warning"
          showIcon
          icon={<WarningOutlined />}
          style={{ marginBottom: 24 }}
        />
      )}

      <Card 
        title="当前天气"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setRecordModalVisible(true)}>
            录入天气
          </Button>
        }
      >
        {currentWeather && (
          <div>
            <Row gutter={[32, 16]} align="middle">
              <Col span={6} style={{ textAlign: 'center' }}>
                {getWeatherIcon(currentWeather.condition)}
                <div style={{ marginTop: 8, fontSize: 16, fontWeight: 'bold' }}>
                  {getConditionText(currentWeather.condition)}
                </div>
                {currentWeather.abnormal && (
                  <Tag color="red" icon={<WarningOutlined />}>
                    天气异常
                  </Tag>
                )}
              </Col>
              <Col span={18}>
                <Descriptions column={3} bordered>
                  <Descriptions.Item label="温度">
                    <span style={{ fontSize: 24, fontWeight: 'bold', color: '#1890ff' }}>
                      {currentWeather.temperature.toFixed(1)}°C
                    </span>
                  </Descriptions.Item>
                  <Descriptions.Item label="湿度">
                    <span style={{ fontSize: 24, fontWeight: 'bold', color: '#722ed1' }}>
                      {currentWeather.humidity.toFixed(1)}%
                    </span>
                  </Descriptions.Item>
                  <Descriptions.Item label="风速">
                    <span style={{ fontSize: 24, fontWeight: 'bold', color: '#52c41a' }}>
                      {currentWeather.windSpeed.toFixed(1)} m/s
                    </span>
                  </Descriptions.Item>
                  <Descriptions.Item label="风向" span={3}>
                    {currentWeather.windDirection}
                  </Descriptions.Item>
                </Descriptions>
              </Col>
            </Row>
          </div>
        )}
      </Card>

      <Divider />

      <Card title="天气影响说明">
        <h4>天气异常对赛事的影响：</h4>
        <Row gutter={[16, 16]}>
          <Col span={12}>
            <Card size="small" title="室外赛事影响">
              <ul>
                <li><strong>下雨/暴风雨：</strong>室外赛事自动取消，全额退款</li>
                <li><strong>下雪：</strong>室外赛事自动取消，全额退款</li>
                <li><strong>大雾：</strong>室外赛事建议改期或取消</li>
                <li><strong>极端高温/低温：</strong>室外赛事建议改期</li>
              </ul>
            </Card>
          </Col>
          <Col span={12}>
            <Card size="small" title="室内赛事影响">
              <ul>
                <li><strong>不受影响：</strong>室内赛事不受天气影响</li>
                <li><strong>正常进行：</strong>所有已预约的室内赛事如期举行</li>
                <li><strong>如需取消：</strong>需按团队取消政策处理</li>
              </ul>
            </Card>
          </Col>
        </Row>
      </Card>

      <Card title="定时任务说明" style={{ marginTop: 24 }}>
        <h4>系统自动检查任务：</h4>
        <ul>
          <li><strong>每日 6:00：</strong>执行每日天气检查，判断是否需要取消室外赛事</li>
          <li><strong>每小时：</strong>执行小时级天气检查，实时监测天气变化</li>
          <li><strong>自动处理：</strong>检测到异常天气时，自动取消当天的室外赛事并全额退款</li>
        </ul>
      </Card>

      <Modal
        title="录入天气记录"
        open={recordModalVisible}
        onOk={handleRecordSubmit}
        onCancel={() => setRecordModalVisible(false)}
      >
        <Form form={recordForm} layout="vertical">
          <Form.Item
            name="date"
            label="日期"
          >
            {/* 简单使用文字输入代替日期选择器 */}
            <Input defaultValue={dayjs().format('YYYY-MM-DD')} placeholder="格式: YYYY-MM-DD" />
          </Form.Item>
          <Form.Item
            name="condition"
            label="天气状况"
            rules={[{ required: true, message: '请选择天气状况' }]}
          >
            <Select placeholder="请选择天气状况">
              {weatherConditionOptions.map(opt => (
                <Option key={opt.value} value={opt.value}>{opt.label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
            rules={[{ required: true, message: '请输入描述' }]}
          >
            <Input placeholder="请输入天气描述" />
          </Form.Item>
          <Form.Item
            name="temperature"
            label="温度(°C)"
            rules={[{ required: true, message: '请输入温度' }]}
          >
            <InputNumber placeholder="请输入温度" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="humidity"
            label="湿度(%)"
            rules={[{ required: true, message: '请输入湿度' }]}
          >
            <InputNumber min={0} max={100} placeholder="请输入湿度" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="windDirection"
            label="风向"
          >
            <Select placeholder="请选择风向">
              <Option value="北风">北风</Option>
              <Option value="南风">南风</Option>
              <Option value="东风">东风</Option>
              <Option value="西风">西风</Option>
              <Option value="东北风">东北风</Option>
              <Option value="东南风">东南风</Option>
              <Option value="西北风">西北风</Option>
              <Option value="西南风">西南风</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="windSpeed"
            label="风速(m/s)"
          >
            <InputNumber min={0} placeholder="请输入风速" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WeatherPage;