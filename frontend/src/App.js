import React, { useState } from 'react';
import { Layout, Menu, theme } from 'antd';
import { 
  HomeOutlined, 
  ApartmentOutlined, 
  CalendarOutlined, 
  UserOutlined, 
  SafetyOutlined,
  CloudOutlined,
  BarChartOutlined
} from '@ant-design/icons';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import StadiumList from './pages/StadiumList';
import MatchList from './pages/MatchList';
import RefereeList from './pages/RefereeList';
import InsurancePage from './pages/InsurancePage';
import WeatherPage from './pages/WeatherPage';
import ReportsPage from './pages/ReportsPage';

const { Header, Sider, Content } = Layout;

const App = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首页概览',
    },
    {
      key: '/stadiums',
      icon: <ApartmentOutlined />,
      label: '场馆管理',
    },
    {
      key: '/matches',
      icon: <CalendarOutlined />,
      label: '赛事预约',
    },
    {
      key: '/referees',
      icon: <UserOutlined />,
      label: '裁判管理',
    },
    {
      key: '/insurance',
      icon: <SafetyOutlined />,
      label: '保险管理',
    },
    {
      key: '/weather',
      icon: <CloudOutlined />,
      label: '天气监控',
    },
    {
      key: '/reports',
      icon: <BarChartOutlined />,
      label: '统计报表',
    },
  ];

  const handleMenuClick = (e) => {
    navigate(e.key);
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        collapsible 
        collapsed={collapsed} 
        onCollapse={(value) => setCollapsed(value)}
        theme="dark"
      >
        <div style={{ 
          height: 64, 
          margin: 16, 
          background: 'rgba(255, 255, 255, 0.2)',
          borderRadius: 8,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontSize: collapsed ? 12 : 16,
          fontWeight: 'bold'
        }}>
          {collapsed ? '场馆' : '体育场馆预约系统'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{ 
          padding: '0 24px', 
          background: colorBgContainer,
          display: 'flex',
          alignItems: 'center',
          fontSize: 18,
          fontWeight: 'bold'
        }}>
          体育场馆预约管理系统
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/stadiums" element={<StadiumList />} />
            <Route path="/matches" element={<MatchList />} />
            <Route path="/referees" element={<RefereeList />} />
            <Route path="/insurance" element={<InsurancePage />} />
            <Route path="/weather" element={<WeatherPage />} />
            <Route path="/reports" element={<ReportsPage />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
};

export default App;