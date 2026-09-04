export default function Tooltip({ text, children, position = "bottom" }) {
  return (
    <span className={`tooltip-wrap tooltip-${position}`}>
      {children}
      <span className="tooltip" role="tooltip">
        {text}
      </span>
    </span>
  );
}
