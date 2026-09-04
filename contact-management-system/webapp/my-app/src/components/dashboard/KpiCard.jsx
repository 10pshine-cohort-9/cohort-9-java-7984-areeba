import Card from "../common/Card";
import { Icons } from "../common/Icons";

export default function KpiCard({ icon: Icon, value, label, trend }) {
  return (
    <Card hover className="kpi-card">
      <div className="kpi-icon">
        <Icon size={22} />
      </div>
      <p className="kpi-value">{value}</p>
      <p className="kpi-label">{label}</p>
      {trend && <p className="kpi-trend">{trend}</p>}
    </Card>
  );
}
