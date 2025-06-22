import React, { useState } from 'react';
import './SideBarView.css';

interface SideOption {
  label: string;
  component: React.ReactNode;
}

interface SideBarProps {
  options: SideOption[];
  title?: string;
}

const SideBarView: React.FC<SideBarProps> = ({ options, title }) => {
  const [activeIndex, setActiveIndex] = useState(0);

  return (
    <div className="sidebar-view">
      <nav className="sidebar-view__nav">
        {title && <div className="sidebar-view__title">{title}</div>}
        {options.map((opt, i) => (
          <button
            key={i}
            className={
              `sidebar-view__button` +
              (activeIndex === i ? ' sidebar-view__button--active' : '')
            }
            onClick={() => setActiveIndex(i)}
          >
            {opt.label}
          </button>
        ))}
      </nav>
      <section className="sidebar-view__content">
        {options[activeIndex].component}
      </section>
    </div>
  );
};

export default SideBarView;
