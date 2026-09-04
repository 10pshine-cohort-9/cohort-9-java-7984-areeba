export default function Button({
  children,
  variant = "primary",
  className = "",
  icon: Icon,
  type = "button",
  ...props
}) {
  return (
    <button type={type} className={`btn btn-${variant} ${className}`} {...props}>
      {Icon && <Icon size={18} />}
      {children && <span>{children}</span>}
    </button>
  );
}
