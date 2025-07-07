import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';
import './PropertiesView.css';

interface Property {
  id: string;
  serverInstanceId: string;
  hidden: boolean;
  type: 'boolean' | 'string' | 'integer';
  value: string;
  key: string;
}

const PropertiesView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [properties, setProperties] = useState<Property[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [changedProps, setChangedProps] = useState<Set<string>>(new Set());
  const toast = useToast();

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    authenticatedFetch
      .get<Property[]>(`/server-instances/${id}/properties`)
      .then(({ data }) => setProperties(data))
      .catch(err => {
        console.error(err);
        toast(err.response?.data?.error || 'Failed to load properties', 'error');
      })
      .finally(() => setLoading(false));
  }, [id, toast]);

  const handleChange = (propertyId: string, newValue: string) => {
    if (!properties) return;
    setProperties(properties.map(p =>
      p.id === propertyId ? { ...p, value: newValue } : p
    ));
    setChangedProps(prev => new Set(prev).add(propertyId));
  };

  const saveProperty = async (propertyId: string) => {
    const prop = properties?.find(p => p.id === propertyId);
    if (!prop) return;
    await authenticatedFetch
      .post<Property[]>(`/server-instances/${id}/property/${propertyId}`, { value: prop.value })
      // .then(() => toast(`Updated ${prop.key}`, 'success'))
      .catch(err => {
        console.error(err);
        toast(err.response?.data?.error || `Failed to save ${prop.key}`, 'error');
      });
  };

  const writeProperties = async () => {
    if (!properties) return;
    await authenticatedFetch
      .post<Property[]>(`/server-instances/${id}/write-properties`)
      .then(() => toast('All properties saved', 'success'))
      .catch(err => {
        console.error(err);
        toast(err.response?.data?.error || 'Failed to save properties', 'error');
      });
  };

  const saveAllProperties = async () => {
    if (!properties || changedProps.size === 0) return;
    // Save all changed properties to the database in parallel
    await Promise.all(
      Array.from(changedProps).map(propertyId => saveProperty(propertyId))
    );
    // After database updates complete, write properties to file
    await writeProperties();
    setChangedProps(new Set());
  };

  if (loading) {
    return <p>Loading properties…</p>;
  }

  if (!properties || properties.length === 0) {
    return <p>No properties found.</p>;
  }

  return (
    <form
      className="properties-container"
      onSubmit={e => {
        e.preventDefault();
        saveAllProperties();
      }}
    >
      <h2>Properties</h2>
      {['boolean', 'integer', 'string'].map(type => {
        const group = properties!.filter(p => p.type === type);
        if (group.length === 0) return null;
        return (
          <fieldset key={type} className="form-section">
            <legend>{type.charAt(0).toUpperCase() + type.slice(1)}</legend>
            <div className="grid-container">
              {group.map(prop => (
                <div key={prop.id} className="property-row">
                  <label htmlFor={prop.id}>{prop.key}</label>
                  {prop.type === 'boolean' ? (
                    <input
                      id={prop.id}
                      type="checkbox"
                      checked={prop.value === 'true'}
                      onChange={e => handleChange(prop.id, e.target.checked ? 'true' : 'false')}
                    />
                  ) : prop.type === 'integer' ? (
                    <input
                      id={prop.id}
                      type="number"
                      value={prop.value}
                      onChange={e => handleChange(prop.id, e.target.value)}
                    />
                  ) : (
                    <input
                      id={prop.id}
                      type="text"
                      value={prop.value}
                      onChange={e => handleChange(prop.id, e.target.value)}
                    />
                  )}
                </div>
              ))}
            </div>
          </fieldset>
        );
      })}
      <button
        type="submit"
        className="primary"
        disabled={changedProps.size === 0}
      >
        Save All
      </button>
    </form>
  );
};

export default PropertiesView;