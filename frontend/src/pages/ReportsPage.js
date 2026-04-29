import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Button, 
  Select,
  message,
  Tag,
  Row,
  Col,
  Statistic,
  Descriptions,
  Divider,
  Progress
} from 'antd';
import { 
  BarChartOutlined, 
  DollarOutlined,
  TeamOutlined,
  SafetyOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { reportApi, matchApi } from '../api';
import dayjs from 'dayjs';

const { Option } = Select;

const ReportsPage = () => {
  const [loading, setLoading] = useState(false);
  const [selectedYear, setSelectedYear] = useState(dayjs().year());
  const [selectedMonth, setSelectedMonth] = useState(dayjs().month() + 1);
  const [report, setReport] = useState(null);
  const [matches, setMatches] = useState([]);

  useEffect(() => {
    fetchReport();
    fetchMatches();
  }, [selectedYear, selectedMonth]);

  const fetchReport = async () => {
    try {
      setLoading(true);
      const res = await reportApi.getMonthly(selectedYear, selectedMonth);
      setReport(res.data);
    } catch (error) {
      console.error('加载报表失败:', error);
      setReport(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchMatches = async () => {
    try {
      const res = await matchApi.getByMonth(selectedYear, selectedMonth);
      setMatches(res.data);
    } catch (error) {
      console.error('加载赛事数据失败:', error);
    }
  };

  const handleGenerateReport = async () => {
    try {
      setLoading(true);
      const res = await reportApi.generate(selectedYear, selectedMonth);
      setReport(res.data);
      message.success('报表生成成功');
    } catch (error) {
      message.error('生成报表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const years = Array.from({ length: 5 }, (_, i) => dayjs().year() - 2 + i);
  const months = Array.from({ length: 12 }, (_, i) => i + 1);

  return (
    <div>
      <div className="page-header">
        <h2>统计报表</h2>
      </div>

      <Card style={{ marginBottom: 24 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col span={8}>
            <Select
              value={selectedYear}
              onChange={setSelectedYear}
              style={{ width: '100%' }}
            >
              {years.map(year => (
                <Option key={year} value={year}>{year}年</Option>
              ))}
            </Select>
          </Col>
          <Col span={8}>
            <Select
              value={selectedMonth}
              onChange={setSelectedMonth}
              style={{ width: '100%' }}
            >
              {months.map(month => (
                <Option key={month} value={month}>{month}月</Option>
              ))}
            </Select>
          </Col>
          <Col span={8}>
            <Button 
              type="primary" 
              icon={<ReloadOutlined />}
              onClick={handleGenerateReport}
              loading={loading}
            >
              生成报表
            </Button>
          </Col>
        </Row>
      </Card>

      {report ? (
        <div>
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col span={6}>
              <Card>
                <Statistic
                  title="总赛事数"
                  value={report.totalMatches}
                  prefix={<BarChartOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="已完成"
                  value={report.completedMatches}
                  prefix={<TeamOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="已取消"
                  value={report.cancelledMatches}
                  prefix={<SafetyOutlined />}
                  valueStyle={{ color: '#ff4d4f' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="取消率"
                  value={report.cancellationRate}
                  suffix="%"
                  valueStyle={{ color: '#faad14' }}
                />
              </Card>
            </Col>
          </Row>

          <Card title="场地利用率">
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <div style={{ marginBottom: 8 }}>
                  总可用小时数: <strong>{report.totalAvailableHours}</strong> 小时
                </div>
                <div style={{ marginBottom: 8 }}>
                  实际使用小时数: <strong>{report.actualUsedHours}</strong> 小时
                </div>
                <div>
                  利用率:
                  <Progress 
                    percent={report.courtUtilizationRate} 
                    status={report.courtUtilizationRate > 70 ? 'active' : 'normal'}
                    style={{ marginTop: 8 }}
                  />
                </div>
              </Col>
              <Col span={12}>
                <div style={{ background: '#f6ffed', padding: 16, borderRadius: 8 }}>
                  <h4 style={{ margin: 0 }}>利用率说明：</h4>
                  <ul style={{ marginTop: 8, marginBottom: 0 }}>
                    <li>利用率 70% 以上：优秀</li>
                    <li>利用率 50% - 70%：良好</li>
                    <li>利用率 30% - 50%：一般</li>
                    <li>利用率 30% 以下：需要改进</li>
                  </ul>
                </div>
              </Col>
            </Row>
          </Card>

          <Divider />

          <Card title="收入统计">
            <Row gutter={[16, 16]}>
              <Col span={6}>
                <Statistic
                  title="场地租金收入"
                  value={report.totalVenueRevenue}
                  prefix="¥"
                  valueStyle={{ color: '#1890ff' }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="裁判费用支出"
                  value={report.totalRefereeFee}
                  prefix="¥"
                  valueStyle={{ color: '#ff4d4f' }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="超时费用收入"
                  value={report.totalOvertimeFee}
                  prefix="¥"
                  valueStyle={{ color: '#52c41a' }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="保险费用收入"
                  value={report.totalInsuranceFee}
                  prefix="¥"
                  valueStyle={{ color: '#722ed1' }}
                />
              </Col>
            </Row>

            <Divider />

            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Card size="small" title="总收入">
                  <div style={{ fontSize: 28, fontWeight: 'bold', color: '#52c41a' }}>
                    ¥{report.totalRevenue}
                  </div>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" title="净收入(扣除退款)">
                  <div style={{ fontSize: 28, fontWeight: 'bold', color: '#1890ff' }}>
                    ¥{report.netRevenue}
                  </div>
                  {report.totalRefund && report.totalRefund.compareTo && report.totalRefund.compareTo(0) !== 0 && (
                    <div style={{ color: '#ff4d4f', marginTop: 4 }}>
                      退款金额: ¥{report.totalRefund}
                    </div>
                  )}
                </Card>
              </Col>
            </Row>
          </Card>
        </div>
      ) : (
        <Card>
          <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
            <BarChartOutlined style={{ fontSize: 64, marginBottom: 16 }} />
            <div>暂无报表数据</div>
            <div style={{ marginTop: 8 }}>
              点击"生成报表"按钮生成{selectedYear}年{selectedMonth}月的报表
            </div>
          </div>
        </Card>
      )}

      <Divider />

      <Card title={`${selectedYear}年${selectedMonth}月赛事列表`}>
        {matches.length > 0 ? (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#fafafa' }}>
                <th style={{ padding: 12, border: '1px solid #f0f0f0' }}>赛事名称</th>
                <th style={{ padding: 12, border: '1px solid #f0f0f0' }}>项目</th>
                <th style={{ padding: 12, border: '1px solid #f0f0f0' }}>日期</th>
                <th style={{ padding: 12, border: '1px solid #f0f0f0' }}>状态</th>
                <th style={{ padding: 12, border: '1px solid #f0f0f0' }}>金额</th>
              </tr>
            </thead>
            <tbody>
              {matches.map(match => (
                <tr key={match.id}>
                  <td style={{ padding: 12, border: '1px solid #f0f0f0' }}>{match.bookingTeamName}</td>
                  <td style={{ padding: 12, border: '1px solid #f0f0f0' }}>{match.sportType}</td>
                  <td style={{ padding: 12, border: '1px solid #f0f0f0' }}>{match.matchDate}</td>
                  <td style={{ padding: 12, border: '1px solid #f0f0f0' }}>
                    <Tag color={
                      match.status === 'COMPLETED' ? 'green' :
                      match.status === 'CANCELLED' ? 'red' :
                      match.status === 'IN_PROGRESS' ? 'cyan' :
                      match.status === 'CONFIRMED' ? 'blue' : 'orange'
                    }>
                      {match.status}
                    </Tag>
                  </td>
                  <td style={{ padding: 12, border: '1px solid #f0f0f0' }}>¥{match.totalAmount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div style={{ textAlign: 'center', color: '#999', padding: 20 }}>
            该月暂无赛事数据
          </div>
        )}
      </Card>

      <Card title="报表说明" style={{ marginTop: 24 }}>
        <h4>报表生成说明：</h4>
        <ul>
          <li><strong>总赛事数：</strong>该月所有创建的赛事数量</li>
          <li><strong>已完成：</strong>状态为"已完成"的赛事数量</li>
          <li><strong>已取消：</strong>状态为"已取消"的赛事数量</li>
          <li><strong>取消率：</strong>已取消赛事占总赛事的百分比</li>
          <li><strong>场地利用率：</strong>实际使用小时数占总可用小时数的百分比</li>
          <li><strong>总收入：</strong>场地租金 + 超时费用 + 保险费用</li>
          <li><strong>净收入：</strong>总收入 - 退款金额</li>
        </ul>

        <h4 style={{ marginTop: 16 }}>定时任务：</h4>
        <ul>
          <li>每月1日凌晨2点自动生成上月报表</li>
          <li>也可手动点击"生成报表"按钮生成指定月份的报表</li>
        </ul>
      </Card>
    </div>
  );
};

export default ReportsPage;