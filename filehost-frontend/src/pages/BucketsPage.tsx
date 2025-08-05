import React, { useState } from 'react'
import { toast } from 'react-toastify'
import { useNavigate } from 'react-router-dom'
import { useBuckets, useCreateBucket, useDeleteBucket } from '../hooks/hooks'
import type { Bucket } from '../types'
import { format } from 'date-fns'
import { FolderIcon, Plus, Trash2, Eye } from 'lucide-react'

const BucketsPage: React.FC = () => {
  const navigate = useNavigate()
  const { data: buckets, isLoading, isError, error, refetch } = useBuckets()
  const createBucketMutation = useCreateBucket()
  const deleteBucketMutation = useDeleteBucket()

  const [newBucketName, setNewBucketName] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  const filteredBuckets = buckets?.filter(bucket =>
    bucket.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    bucket.apiKey.toLowerCase().includes(searchTerm.toLowerCase()) ||
    bucket.path.toLowerCase().includes(searchTerm.toLowerCase())
  ) || []

  const handleCreateBucket = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newBucketName) {
      toast.warn('Por favor, ingresa el nombre del bucket.')
      return
    }
    try {
      await createBucketMutation.mutateAsync({ name: newBucketName })
      toast.success('¡Bucket creado con éxito! 🎉')
      setNewBucketName('')
      refetch()
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Error al crear el bucket.')
    }
  }

  const handleDeleteBucket = async (apiKey: string, bucketName: string) => {
    if (!window.confirm(`¿Eliminar el bucket "${bucketName}"? Esta acción no se puede deshacer.`)) return
    try {
      await deleteBucketMutation.mutateAsync({ apiKey })
      toast.success(`Bucket "${bucketName}" eliminado.`)
      refetch()
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Error al eliminar el bucket.')
    }
  }

  const handleViewFiles = (apiKey: string) => {
    navigate(`/files/${apiKey}`)
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-secondary">
        <p className="text-primary">Cargando buckets...</p>
      </div>
    )
  }

  if (isError) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-secondary">
        <p className="text-red-500">Error: {error?.message}</p>
      </div>
    )
  }
  const notify = () => {
    toast('This is a simple toast message!');
  };

  return (
    <div className="min-h-screen bg-secondary p-6">
      <header className="flex items-center mb-6">
        <FolderIcon className="w-8 h-8 text-primary mr-2" />
        <h1 className="text-2xl font-bold text-primary">Buckets</h1>
      </header>
    <button onClick={notify}>Show Toast</button> a

      <form onSubmit={handleCreateBucket} className="bg-card border border-border rounded-xl p-4 mb-6 space-y-4 shadow">
        <h2 className="text-lg font-semibold text-primary flex items-center gap-2">
          <Plus className="w-5 h-5" /> Crear Bucket
        </h2>
        <div className="flex flex-col md:flex-row items-center gap-4">
          <input
            type="text"
            placeholder="Nombre del bucket"
            value={newBucketName}
            onChange={(e) => setNewBucketName(e.target.value)}
            className="flex-1 py-2 px-3 rounded-lg border border-border bg-secondary text-primary"
            required
          />
          <button
            type="submit"
            disabled={createBucketMutation.isPending}
            className="bg-primary text-primary-foreground px-4 py-2 rounded-lg hover:bg-primary/90 transition"
          >
            {createBucketMutation.isPending ? 'Creando...' : 'Crear'}
          </button>
        </div>
      </form>

      <div className="bg-card border border-border rounded-xl p-4 mb-6 shadow">
        <input
          type="text"
          placeholder="Buscar buckets..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full py-2 px-3 rounded-lg border border-border bg-secondary text-primary"
        />
      </div>

      <div className="space-y-4">
        {filteredBuckets.length > 0 ? (
          filteredBuckets.map((bucket: Bucket) => (
            <div key={bucket.id} className="bg-card border border-border rounded-xl p-4 shadow flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
              <div className="space-y-1">
                <h3 className="text-lg font-bold text-primary">{bucket.name}</h3>
                <p className="text-sm text-muted-foreground">
                  <strong>Ruta:</strong> {bucket.path}
                </p>
                <p className="text-sm text-muted-foreground break-all">
                  <strong>API Key:</strong> <span className="font-mono">{bucket.apiKey}</span>
                </p>
                <p className="text-xs text-muted-foreground">
                  Creado: {format(new Date(bucket.createdAt), 'dd/MM/yyyy HH:mm')}
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => handleViewFiles(bucket.apiKey)}
                  className="px-3 py-1 text-sm rounded-md bg-primary text-primary-foreground hover:bg-primary/90 flex items-center gap-1"
                >
                  <Eye className="w-4 h-4" /> Ver
                </button>
                <button
                  onClick={() => handleDeleteBucket(bucket.apiKey, bucket.name)}
                  disabled={deleteBucketMutation.isPending && deleteBucketMutation.variables?.apiKey === bucket.apiKey}
                  className="px-3 py-1 text-sm rounded-md bg-destructive text-white hover:bg-destructive/90 flex items-center gap-1"
                >
                  <Trash2 className="w-4 h-4" />
                  {deleteBucketMutation.isPending && deleteBucketMutation.variables?.apiKey === bucket.apiKey
                    ? 'Eliminando...'
                    : 'Eliminar'}
                </button>
              </div>
            </div>
          ))
        ) : (
          <p className="text-muted-foreground text-center">
            {searchTerm
              ? 'No se encontraron buckets. 🧐'
              : 'No tienes buckets todavía. ¡Crea uno! 💡'}
          </p>
        )}
      </div>
    </div>
  )
}

export default BucketsPage
