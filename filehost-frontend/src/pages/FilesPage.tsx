import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { useQueryClient } from '@tanstack/react-query'
import {
  useFiles,
  useUploadFile,
  useUploadManyFiles,
  useDeleteFile,
  useDeleteManyFiles,
  useDownloadFile,
  useViewFile,
} from '../hooks/hooks'
import type { FileDTO } from '../types'
import { format } from 'date-fns'
import {
  ArrowLeft,
  Folder,
  UploadCloud,
  Upload,
  Trash2,
  Download,
  Eye,

  X,
  Loader2,
} from 'lucide-react'

const isViewable = (mime: string) =>
  mime.startsWith('image/') || mime.startsWith('video/')

const FilesPage: React.FC = () => {
  const { bucketKey } = useParams<{ bucketKey: string }>()
  const queryClient = useQueryClient()

  // --- QUERIES Y MUTATIONS ---
  const { data, isPending: loadingFiles, isError, error, refetch } = useFiles(bucketKey || '')
  const files: FileDTO[] = data ?? []

  const uploadSingle = useUploadFile()
  const uploadMany = useUploadManyFiles()
  const deleteSingle = useDeleteFile()
  const deleteMany = useDeleteManyFiles()
  const downloadFile = useDownloadFile()

  // --- ESTADO LOCAL ---
  const [selFile, setSelFile] = useState<File | null>(null)
  const [selFiles, setSelFiles] = useState<FileList | null>(null)
  const [selIds, setSelIds] = useState<number[]>([])
  const [batchProg, setBatchProg] = useState<{ done: number; total: number } | null>(null)

  const [viewingFile, setViewingFile] = useState<{ id: number; mime: string } | null>(null)
  const [viewModal, setViewModal] = useState<{ open: boolean; url?: string }>({ open: false })

  const { data: viewData, isLoading: isViewLoading } = useViewFile(
    viewingFile?.id ?? null,
    bucketKey || ''
  )

  // --- EFECTOS ---
  useEffect(() => {
    if (isViewLoading) {
      // Abre el modal en estado de carga inmediatamente
      setViewModal({ open: true, url: undefined });
      return;
    }

    if (viewData && viewingFile) {
      const blob = new Blob([viewData], { type: viewingFile.mime })
      const url = URL.createObjectURL(blob)
      setViewModal({ open: true, url })
    } else if (!viewingFile) {
      // Si el usuario cierra el modal, viewingFile se pone a null y esto se ejecuta
      setViewModal({ open: false, url: undefined })
    }
  }, [viewData, isViewLoading, viewingFile])

  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (uploadMany.isPending) {
        e.preventDefault()
        e.returnValue = 'Subidas en curso, ¿seguro que quieres salir?'
      }
    }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [uploadMany.isPending])

  // --- MANEJADORES DE EVENTOS ---
  const invalidateFilesQuery = () => {
    queryClient.invalidateQueries({ queryKey: ['files', bucketKey] })
  }

  const handleUploadFile = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!bucketKey || !selFile) return

    try {
      const response = await uploadSingle.mutateAsync({ bucketKey, file: selFile })
      console.log(response);

      if (response.code === "201") {
        toast.success(`"${selFile.name}" subido con éxito`)
        invalidateFilesQuery()
        refetch()

        setSelFile(null);
        // Doesn´t work | 
        //              | 
        //              v
        // (e.currentTarget as HTMLFormElement).reset()
      } else {
        toast.error('Error al subir el archivo')
      }
    } catch (err) {
      console.error(err);
      toast.error('Error al subir el archivo')
    }
  }

  const handleUploadMany = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!bucketKey || !selFiles) return
    const arr = Array.from(selFiles)
    setBatchProg({ done: 0, total: arr.length })
    try {
      await uploadMany.mutateAsync({
        bucketKey,
        files: arr,
        batchSize: 5,
        onBatchProgress: (done, total) => setBatchProg({ done, total }),
      })
      toast.success(`¡${arr.length} archivos subidos!`)
      invalidateFilesQuery()
      setSelFiles(null);
      (e.currentTarget as HTMLFormElement).reset()
    } catch {
      toast.error('Error en la subida múltiple')
    } finally {
      setBatchProg(null)
    }
  }

  const handleDelete = async (ids: number[]) => {
    if (!bucketKey || ids.length === 0) return
    if (!window.confirm(`¿Seguro que quieres eliminar ${ids.length} archivo(s)?`)) return

    const isMany = ids.length > 1
    const mutation = isMany ? deleteMany : deleteSingle
    const params = isMany ? { bucketKey, ids } : { bucketKey, id: ids[0] }

    toast.promise(
      // @ts-ignore
      mutation.mutateAsync(params).then(() => {
        setSelIds(prev => prev.filter(id => !ids.includes(id)))
        invalidateFilesQuery()
      }),
      {
        pending: 'Eliminando...',
        success: 'Eliminación completada',
        error: 'Error al eliminar',
      }
    )
  }

  const handleDownload = (file: FileDTO) => {
    if (!bucketKey) return
    toast.promise(
      downloadFile.mutateAsync({ id: file.id, apiKey: bucketKey, fileName: file.originalName }),
      {
        pending: `Descargando "${file.originalName}"...`,
        success: '¡Descarga iniciada!',
        error: `Error al descargar "${file.originalName}"`,
      }
    )
  }

  const handleCloseViewModal = () => {
    if (viewModal.url) {
      URL.revokeObjectURL(viewModal.url)
    }
    setViewingFile(null)
  }

  const toggleSelect = (id: number) => {
    setSelIds(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    )
  }

  const toggleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      setSelIds(files.map(f => f.id));
    } else {
      setSelIds([]);
    }
  }


  // --- RENDERIZADO ---
  if (loadingFiles) return <LoadingView />
  if (isError) return <ErrorView message={error?.message} />
  if (!bucketKey) return <ErrorView message="Bucket key no disponible." />

  return (
    <div className="min-h-screen bg-secondary p-6 space-y-6">
      <header className="flex items-center gap-2">
        <Link to="/buckets" className="text-primary hover:text-primary/80">
          <ArrowLeft size={24} />
        </Link>
        <Folder size={24} className="text-primary" />
        <h1 className="text-2xl font-bold text-primary break-words">Archivos de {bucketKey}</h1>
      </header>

      <section className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <UploadFormSingle
          selFile={selFile}
          setSelFile={setSelFile}
          onSubmit={handleUploadFile}
          loading={uploadSingle.isPending}
        />
        <UploadFormMany
          selFiles={selFiles}
          setSelFiles={setSelFiles}
          onSubmit={handleUploadMany}
          loading={uploadMany.isPending}
          batchProg={batchProg}
        />
      </section>

      {files.length > 0 && (
        <div className="flex justify-start">
          {selIds.length > 0 && (
            <button
              onClick={() => handleDelete(selIds)}
              disabled={deleteMany.isPending || deleteSingle.isPending}
              className="bg-destructive text-destructive-foreground px-4 py-2 rounded-md hover:bg-destructive/90 transition flex items-center gap-2"
            >
              <Trash2 size={16} />
              Eliminar ({selIds.length})
            </button>
          )}
        </div>
      )}

      <div className="overflow-auto bg-card border border-border rounded-lg">
        <table className="w-full table-auto">
          <thead className="bg-secondary text-sm text-primary">
            <tr>
              <th className="p-2 text-center w-12">
                <input
                  type="checkbox"
                  className="form-checkbox"
                  checked={files.length > 0 && selIds.length === files.length}
                  onChange={toggleSelectAll}
                />
              </th>
              <th className="p-2 text-left">Nombre</th>
              <th className="p-2 text-left">Ruta</th>
              <th className="p-2 text-left">Tipo</th>
              <th className="p-2 text-right">Tamaño</th>
              <th className="p-2 text-left">Subida</th>
              <th className="p-2 text-center">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {files.map(f => (
              <tr key={f.id} className="border-t border-border hover:bg-secondary/50">
                <td className="p-2 text-center">
                  <input
                    type="checkbox"
                    className="form-checkbox"
                    checked={selIds.includes(f.id)}
                    onChange={() => toggleSelect(f.id)}
                  />
                </td>
                <td className="p-2 text-primary">{f.originalName}</td>
                <td className="p-2 text-xs text-primary/80 break-all">{f.relativePath}</td>
                <td className="p-2 text-primary">{f.mimeType.split('/')[1] ?? f.mimeType}</td>
                <td className="p-2 text-right text-primary">{Math.round(f.sizeBytes / 1024)}&nbsp;KB</td>
                <td className="p-2 text-xs text-primary/80">{format(new Date(f.uploadTime), 'dd/MM/yyyy HH:mm')}</td>
                <td className="p-2 flex justify-center gap-3">
                  {isViewable(f.mimeType) && (
                    <button onClick={() => setViewingFile({ id: f.id, mime: f.mimeType })}>
                      <Eye size={18} className="text-primary hover:text-primary/80" />
                    </button>
                  )}
                  <button onClick={() => handleDownload(f)}>
                    <Download size={18} className="text-primary hover:text-primary/80" />
                  </button>
                  <button onClick={() => handleDelete([f.id])}>
                    <Trash2 size={18} className="text-destructive hover:text-destructive/80" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {files.length === 0 && (
          <p className="text-center text-primary/70 p-8">Este bucket está vacío. ¡Sube algunos archivos!</p>
        )}
      </div>

      {viewModal.open && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4" onClick={handleCloseViewModal}>
          <div className="bg-card rounded-lg p-2 relative max-w-4xl max-h-[90vh]" onClick={(e) => e.stopPropagation()}>
            <button onClick={handleCloseViewModal} className="absolute -top-2 -right-2 bg-card rounded-full p-1 z-10">
              <X size={24} />
            </button>

            {(isViewLoading || !viewModal.url) && (
              <div className="w-96 h-96 flex items-center justify-center">
                <Loader2 className="animate-spin text-primary" size={48} />
              </div>
            )}

            {viewModal.url && viewingFile?.mime.startsWith('image/') && (
              <img src={viewModal.url} alt="preview" className="block max-w-full max-h-[85vh] object-contain" />
            )}
            {viewModal.url && viewingFile?.mime.startsWith('video/') && (
              <video controls autoPlay src={viewModal.url} className="block max-w-full max-h-[85vh]" />
            )}
          </div>
        </div>
      )}
    </div>
  )
}

// --- COMPONENTES AUXILIARES ---

const LoadingView = () => (
  <div className="min-h-screen flex items-center justify-center bg-secondary">
    <Loader2 className="animate-spin text-primary" size={48} />
  </div>
)

const ErrorView = ({ message }: { message?: string }) => (
  <div className="min-h-screen flex flex-col items-center justify-center bg-secondary p-4">
    <p className="text-destructive">{message || 'Error inesperado'}</p>
    <Link to="/buckets" className="mt-4 text-primary hover:underline">
      Volver a Buckets
    </Link>
  </div>
)

const UploadFormSingle = ({ selFile, setSelFile, onSubmit, loading }: { selFile: File | null; setSelFile: (f: File | null) => void; onSubmit: (e: React.FormEvent) => void; loading: boolean; }) => (
  <div className="bg-card border border-border rounded-lg p-4 space-y-2">
    <div className="flex items-center gap-2 text-primary font-semibold">
      <UploadCloud size={20} /> Subida Individual
    </div>
    <form onSubmit={onSubmit} className="flex gap-2">
      <input
        type="file"
        onChange={e => setSelFile(e.target.files?.[0] ?? null)}
        className="flex-1 file:py-1 file:px-3 file:border-0 file:bg-secondary file:text-primary file:rounded-md"
      />
      <button
        type="submit"
        disabled={!selFile || loading}
        className="bg-primary text-primary-foreground px-4 py-1 rounded hover:bg-primary/90 transition disabled:opacity-50"
      >
        <Upload size={16} className="inline mr-1" />
        {loading ? 'Subiendo…' : 'Subir'}
      </button>
    </form>
  </div>
)

const UploadFormMany = ({ selFiles, setSelFiles, onSubmit, loading, batchProg }: { selFiles: FileList | null; setSelFiles: (f: FileList | null) => void; onSubmit: (e: React.FormEvent) => void; loading: boolean; batchProg: { done: number; total: number } | null; }) => (
  <div className="bg-card border border-border rounded-lg p-4 space-y-2">
    <div className="flex items-center gap-2 text-primary font-semibold">
      <UploadCloud size={20} /> Subida Múltiple
    </div>
    <form onSubmit={onSubmit} className="flex flex-col gap-3">
      <input
        type="file"
        multiple
        onChange={e => setSelFiles(e.target.files)}
        className="file:py-1 file:px-3 file:border-0 file:bg-secondary file:text-primary file:rounded-md"
      />
      <button
        type="submit"
        disabled={!selFiles?.length || loading}
        className="bg-primary text-primary-foreground px-4 py-1 rounded hover:bg-primary/90 transition disabled:opacity-50"
      >
        <Upload size={16} className="inline mr-1" />
        {loading ? `Subiendo ${batchProg?.done}/${batchProg?.total}...` : `Subir ${selFiles?.length || 0}`}
      </button>
      {loading && batchProg && (
        <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
          <div
            className="bg-primary h-2 transition-all"
            style={{ width: `${(batchProg.done / batchProg.total) * 100}%` }}
          />
        </div>
      )}
    </form>
  </div>
)

export default FilesPage