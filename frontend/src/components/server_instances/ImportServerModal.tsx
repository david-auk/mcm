import React, { useState, useEffect } from 'react';
// NOTE: This component uses JSZip. Make sure `jszip` is installed: `npm i jszip`
import Modal from '../shared/views/Modal';
import ServerInstanceModal from './ServerInstanceModal';
import { useToast } from '../../contexts/ToastContext';
import type ServerInstance from '../../pages/server_instance/ServerInstance';

interface ImportServerModalProps {
    isOpen: boolean;
    onClose: () => void;
    /** Called after a successful finalize to refresh the server list */
    onImported: () => void;
}

const ImportServerModal: React.FC<ImportServerModalProps> = ({ isOpen, onClose, onImported }) => {
    const toast = useToast();
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [importing, setImporting] = useState(false);
    const [showEdit, setShowEdit] = useState(false);
    const [draftServer, setDraftServer] = useState<ServerInstance | null>(null);

    useEffect(() => {
        if (draftServer !== null) {
            console.log('[draftServer updated]', draftServer);
        }
    }, [draftServer]);

    /** Step 1: Read ZIP on the client, extract mcm_metadata.json, and build a draft ServerInstance */
    const handlePreview = async () => {
        const file = selectedFile;
        if (!file) {
            toast('Please select a .zip file', 'error');
            return;
        }
        setImporting(true);
        try {
            // Lazy-load JSZip to avoid adding weight on initial load
            const JSZip = (await import('jszip')).default;
            const arrayBuffer = await file.arrayBuffer();
            const zip = await JSZip.loadAsync(arrayBuffer);

            // Find metadata file (root or nested). Case-insensitive endsWith.
            const metaEntryName = Object.keys(zip.files).find((k) => k.toLowerCase().endsWith('mcm_metadata.json'));
            if (!metaEntryName) {
                throw new Error('mcm_metadata.json not found in the archive');
            }
            const metaText = await zip.file(metaEntryName)!.async('string');
            const meta = JSON.parse(metaText);

            // Map metadata to ServerInstance shape, ignoring unknown fields
            const draft: ServerInstance = {
                id: meta.id ?? undefined,
                name: meta.name ?? undefined,
                description: meta.description ?? null, // fallback to null
                minecraftVersion: meta.minecraftVersion ?? meta.minecraft_version ?? null,
                jarUrl: meta.jarUrl ?? null,
                eulaAccepted: false, // force false on import
                createdAt: meta.createdAt ?? null,
                running: false,
                allocatedRamMB: meta.allocatedRamMb ?? 1024,
                port: meta.port ?? null
            };

            setDraftServer(draft);
            setShowEdit(true);
        } catch (err: any) {
            console.error(err);
            toast(err.message || 'Failed to read export.zip', 'error');
        } finally {
            setImporting(false);
        }
    };

    /** Step 2: Finalize the import using the edited server + original file + stagingToken */
    const handleFinalize = async () => {
        try {
            setShowEdit(false);
            setDraftServer(null);
            setSelectedFile(null);
            onImported();
        } catch (err: any) {
            console.error(err);
            toast(err.response?.data?.error || 'Import finalize failed', 'error');
        }
    };

    return (
        <>
            {isOpen && !showEdit && (
                <Modal
                    title="Import Server Instance"
                    onClose={onClose}
                    onConfirm={handlePreview}
                    confirmText={importing ? 'Importing…' : 'Import'}
                // confirmDisabled={importing}
                >
                    <p>Select an exported <code>export.zip</code> to import.</p>
                    <input
                        id="import-file"
                        type="file"
                        accept=".zip,application/zip"
                        onChange={(e) => setSelectedFile(e.target.files?.[0] ?? null)}
                    />
                    <p className="note">⚠️ User data and permissions are not included in exports and will not be imported.</p>
                </Modal>
            )}

            {showEdit && draftServer && (
                <ServerInstanceModal
                    server={draftServer}
                    isOpen={showEdit}
                    onClose={() => {
                        setShowEdit(false);
                        setDraftServer(null);
                        setSelectedFile(null);
                    }}
                    onSaved={handleFinalize}
                    forumMode={'import'}
                    importedFile={selectedFile}
                />
            )}
        </>
    );
};

export default ImportServerModal;