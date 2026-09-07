import { Icons } from "../common/Icons";

const TYPE_CONFIG = {
  business: { label: "Business", icon: Icons.Briefcase, className: "badge-business" },
  personal: { label: "Personal", icon: Icons.User, className: "badge-personal" },
  other: { label: "Other", icon: Icons.Users, className: "badge-other" },
};

export default function ContactTypeBadge({ type }) {
  const config = TYPE_CONFIG[type] || TYPE_CONFIG.other;
  const Icon = config.icon;

  return (
    <span className={`type-badge ${config.className}`}>
      <Icon size={14} />
      <span>{config.label}</span>
    </span>
  );
}
