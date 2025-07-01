import React, { useState } from 'react';
import './TabView.css';

export interface Tab {
  label: string;
  component: React.ReactNode;
}

interface TabViewProps {
  tabs: Tab[];
  title?: string;
  subtitle?: string;
}

const TabView: React.FC<TabViewProps> = ({ tabs, title, subtitle }) => {
  const [activeIndex, setActiveIndex] = useState(0);
  const isInline = !title && !subtitle;

  // If this TabView is “inline” (nested with no header), render only nav + content
  if (isInline) {
    return (
      <>
        <nav className="tab-view__nav tab-view__nav--inline">
          {tabs.map((tab, i) => (
            <button
              key={i}
              className={`tab-view__tab${activeIndex === i ? ' tab-view__tab--active' : ''}`}
              onClick={() => setActiveIndex(i)}
            >
              {tab.label}
            </button>
          ))}
        </nav>
        <section className="tab-view__content">
          {tabs[activeIndex]?.component}
        </section>
      </>
    );
  }

  // Otherwise, full “windowed” TabView
  return (
    <div className="tab-view">
      {(title || subtitle) && (
        <header className="tab-view__header">
          {title && <h2 className="tab-view__title">{title}</h2>}
          {subtitle && <p className="tab-view__subtitle">{subtitle}</p>}
        </header>
      )}

      <nav className="tab-view__nav">
        {tabs.map((tab, i) => (
          <button
            key={i}
            className={`tab-view__tab${activeIndex === i ? ' tab-view__tab--active' : ''}`}
            onClick={() => setActiveIndex(i)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <section className="tab-view__content">
        {tabs[activeIndex]?.component}
      </section>
    </div>
  );
};

export default TabView;
