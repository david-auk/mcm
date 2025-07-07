// TODO (use hypothetical endpoints which later will be constructed)

// Add preview of current custom image (if exists)

// Add a way to remove the current image


// Add a way to add new custom image (logic below)

// Server icons must be: Resized to 64x64 (frontend) and renamed to server-icon.png. (backend)
import React, { useState, useEffect, type ChangeEvent } from 'react';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';
import './ServerImageView.css';

interface ServerImageViewProps {
    serverId: string;
}

const ServerImageView: React.FC<ServerImageViewProps> = ({ serverId }) => {
    const [currentImageUrl, setCurrentImageUrl] = useState<string | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [loading, setLoading] = useState(false);
    const toast = useToast();

    useEffect(() => {
        const fetchImage = async () => {
            await authenticatedFetch.get(`/server-instances/${serverId}/image`, {
                responseType: 'blob'
            })
            .then(response => {
                const url = URL.createObjectURL(response.data);
                setCurrentImageUrl(url);
            })
            .catch(err => {
            if (err.response && err.response.status !== 404) {
                toast('Failed to load image', 'error');
            }});
        };
        fetchImage();
        return () => {
            if (currentImageUrl) URL.revokeObjectURL(currentImageUrl);
            if (previewUrl) URL.revokeObjectURL(previewUrl);
        };
    }, [serverId]);

    const handleRemove = async () => {
        if (!window.confirm('Are you sure you want to remove the current image?')) return;
        try {
            setLoading(true);
            await authenticatedFetch.delete(`/server-instances/${serverId}/image`);
            setCurrentImageUrl(null);
            toast('Removed Server Image', 'success')
        } catch (err) {
            toast('Failed to remove image', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const img = new Image();
        const reader = new FileReader();
        reader.onload = (event) => {
            img.onload = () => {
                const canvas = document.createElement('canvas');
                canvas.width = 64;
                canvas.height = 64;
                const ctx = canvas.getContext('2d');
                if (ctx) {
                    ctx.drawImage(img, 0, 0, 64, 64);
                    canvas.toBlob((blob) => {
                        if (blob) {
                            const resizedFile = new File([blob], 'server-icon.png', { type: 'image/png' });
                            setSelectedFile(resizedFile);
                            const preview = URL.createObjectURL(blob);
                            setPreviewUrl(preview);
                        }
                    }, 'image/png');
                }
            };
            if (event.target?.result) {
                img.src = event.target.result as string;
            }
        };
        reader.readAsDataURL(file);
    };

    const handleUpload = async () => {
        if (!selectedFile) return;
        try {
            setLoading(true);
            const formData = new FormData();
            formData.append('file', selectedFile);
            await authenticatedFetch.post(`/server-instances/${serverId}/image`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setCurrentImageUrl(previewUrl);
            setSelectedFile(null);
            setPreviewUrl(null);
            toast('Updated Server Image', 'success')
        } catch (err) {
            toast('Failed to upload file', 'error')
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="server-image-view">
            <h3>Server Image</h3>
            {loading && <p className="loading">Loading...</p>}
            {currentImageUrl ? (
                <div className="current-image">
                    <p>Current Image:</p>
                    <img src={currentImageUrl} alt='Server' width={64} height={64} />
                    <button className="danger" onClick={handleRemove} disabled={loading}>Remove Image</button>
                </div>
            ) : (
                <p className="no-image">No image set.</p>
            )}
            <div className="upload-section">
                <p>Add / Replace Image:</p>
                <input type='file' accept='image/*' onChange={handleFileChange} disabled={loading} />
                {previewUrl && (
                    <div className="preview">
                        <p>Preview:</p>
                        <img src={previewUrl} alt='Preview' width={64} height={64} />
                        <button className="primary" onClick={handleUpload} disabled={loading}>Upload</button>
                        <button className="secondary" onClick={() => { setPreviewUrl(null); setSelectedFile(null); }}>Cancel</button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ServerImageView;