import Tooltip from "./Tooltip";

export default function IconButton({ icon: Icon, label, className = "", variant = "ghost", ...props }) {
  return (
    <Tooltip text={label} position="bottom">
      <button
        type="button"
        className={`icon-btn icon-btn-${variant} ${className}`}
        aria-label={label}
        {...props}
      >
        <Icon size={18} />
      </button>
    </Tooltip>
  );
}
