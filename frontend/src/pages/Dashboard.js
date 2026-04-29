import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Tag, message, Spin } from 'antd';
import { 
  ApartmentOutlined, 
  CalendarOutlined, 
  UserOutlined, 
  SafetyOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  CloseCircleOutlined
} from '@ant-design/icons';
import { matchApi, stadiumApi, refereeApi, weatherApi } from '../api';
import dayjs from 'dayjs';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalMatches: 0,
    completedMatches: 0,
    cancelledMatches: 0,
    totalStadiums: 0,
    totalReferees: 0,
  });
  const [todayMatches, setTodayMatches] = useState([]);
  const [weather, setWeather] = useState(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      const [matchesRes, stadiumsRes, refereesRes, weatherRes] = await Promise.all([
        matchApi.getAll(),
        stadiumApi.getAll(),
        refereeApi.getAll(),
        weatherApi.getCurrent(dayjs().format('YYYY-MM-DD')),
      ]);

      const matches = matchesRes.data;
      const completed = matches.filter(m => m.status === 'COMPLETED').length;
      const cancelled = matches.filter(m => m.status === 'CANCELLED').length;

      setStats({
        totalMatches: matches.length,
        completedMatches: completed,
        cancelledMatches: cancelled,
        totalStadiums: stadiumsRes.data.length,
        totalReferees: refereesRes.data.length,
      });

      const today = dayjs().format('YYYY-MM-DD');
      const todayMatchesData = matches.filter(m => m.matchDate === today);
      setTodayMatches(todayMatchesData);

      setWeather(weatherRes.data);
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

  const columns = [
    {
      title: '赛事名称',
      dataIndex: 'bookingTeamName',
      key: 'bookingTeamName',
    },
    {
      title: '项目类型',
      dataIndex: 'sportType',
      key: 'sportType',
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
  ];

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h2>首页概览</h2>
      </div>

      {weather && (
        <Card style={{ marginBottom: 24 }}>
          <Row gutter={16}>
            <Col span={24}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            {weather.condition === 'SUNNY' ? '☀️' : 
             weather.condition === 'CLOUDY' ? '⛅' : 
             weather.condition === 'RAINY' ? '🌧️' : 
             weather.condition === 'STORMY' ? '⛈️' : 
             weather.condition === 'SNOWY' ? '❄️' : 
             weather.condition === 'FOGGY' ? '🌫️' : '🌡️'}
            <div>
              <div style={{ fontSize: 16, fontWeight: 'bold' }}>
                {weather.description}
              </div>
              <div style={{ color: '#666' }}>
                温度: {weather.temperature.toFixed(1)}°C | 
                湿度: {weather.humidity.toFixed(1)}%
              </div>
            </div>
            {weather.abnormal && (
              <Tag color="red" icon={<WarningOutlined />}>
                天气异常，室外赛事可能受影响
              </Tag>
            )}
          </div>
            </Col>
          </Row>
        </Card>
      )}

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={12} md={6}>
          <Card>
            <Statistic
              title="总赛事数"
              value={stats.totalMatches}
              prefix={<CalendarOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card>
            <Statistic
              title="已完成"
              value={stats.completedMatches}
              valueStyle={{ color: '#3f8600' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card>
            <Statistic
              title="已取消"
              value={stats.cancelledMatches}
              valueStyle={{ color: '#cf1322' }}
              prefix={<CloseCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card>
            <Statistic
              title="场馆数量"
              value={stats.totalStadiums}
              prefix={<ApartmentOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="今日赛事">
            {todayMatches.length > 0 ? (
              <Table
                dataSource={todayMatches}
                columns={columns}
                rowKey="id"
                pagination={false}
              />
            ) : (
              <div style={{ textAlign: 'center', color: '#999', padding: '20px' }}>
                今日暂无赛事安排
              </div>
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col span={12}>
          <Card>
            <Statistic
              title="裁判数量"
              value={stats.totalReferees}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            <Statistic
              title="取消率"
              value={stats.totalMatches > 0 
                ? ((stats.cancelledMatches / stats.totalMatches) * 100).toFixed(1) 
                : 0}
              suffix="%"
              valueStyle={{ color: '#1890ff' }}
              prefix={<SafetyOutlined />}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;