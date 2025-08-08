import { useQuery, useMutation } from '@tanstack/react-query'
import type {
    UseQueryResult,
    UseMutationResult,
} from '@tanstack/react-query'
import { api } from '../api/client'
import type { Bucket, FileDTO, ApiResponse, LoginResponse } from '../types'

/** — Auth — */
export function useLogin(): UseMutationResult<
    LoginResponse,
    Error,
    { username: string; password: string }
> {
    return useMutation<LoginResponse, Error, { username: string; password: string }>(
        {
            mutationFn: async (creds) => {
                const resp = await api.post<LoginResponse>('/auth/login', creds)
                localStorage.setItem('token', resp.data.token)
                return resp.data
            },
        }
    )
}

/** — Buckets — */
export function useBuckets(): UseQueryResult<Bucket[], Error> {
    return useQuery<Bucket[], Error>({
        queryKey: ['buckets'],
        queryFn: async () => {
            const resp = await api.get<Bucket[]>('/api/buckets/all')
            return resp.data
        },
    })
}


export function useBucketByApiKey(apiKey: string): UseQueryResult<Bucket | null, Error> {
    return useQuery<Bucket | null, Error>({
        queryKey: ['bucket', apiKey],
        queryFn: async () => {
                const resp = await api.get<Bucket>('/api/buckets', { data: { apiKey: apiKey } });
            return resp.data;
        },
        enabled: Boolean(apiKey),
    });
}

export function useCreateBucket(): UseMutationResult<
    ApiResponse<string>,
    Error,
    { name: string; }
> {
    return useMutation<ApiResponse<string>, Error, { name: string }>(
        {
            mutationFn: (data) => api.post<ApiResponse<string>>('/api/buckets/create', data).then(r => r.data),
        }
    )
}

export function useDeleteBucket(): UseMutationResult<
    ApiResponse<null>,
    Error,
    { apiKey: string }
> {
    return useMutation<ApiResponse<null>, Error, { apiKey: string }>(
        {
            mutationFn: async ({ apiKey }) => {
                const resp = await api.delete<ApiResponse<null>>('/api/buckets', { data: { apiKey } });
                return resp.data;
            },
        }
    );
}


/** — Files — */
// Original useFiles: Lists files in a bucket
export function useFiles(bucketKey: string): UseQueryResult<FileDTO[], Error> {
    return useQuery<FileDTO[], Error>({
        queryKey: ['files', bucketKey],
        queryFn: async () => {
            const resp = await api.get<FileDTO[]>('/api/files', { params: { bucketKey } })
            return resp.data
        },
        enabled: Boolean(bucketKey),
    })
}

// Original useUploadFile: Uploads a single file
export function useUploadFile(): UseMutationResult<
    ApiResponse<FileDTO>,
    Error,
    { bucketKey: string; file: File }
> {
    return useMutation<ApiResponse<FileDTO>, Error, { bucketKey: string; file: File }>(
        {
            mutationFn: async ({ bucketKey, file }) => {
                const formData = new FormData()
                formData.append('bucketKey', bucketKey)
                formData.append('file', file)
                const resp = await api.post<ApiResponse<FileDTO>>(
                    '/api/files/upload',
                    formData,
                    { headers: { 'Content-Type': 'multipart/form-data' } }
                )
                return resp.data
            },
        }
    )
}

// Hook for multi-file upload
// src/hooks/hooks.ts

type BatchOptions = {
    bucketKey: string
    files: File[]
    batchSize?: number
    onBatchProgress?: (completed: number, total: number) => void
}

export function useUploadManyFiles() {
    return useMutation<
        FileDTO[],
        Error,
        BatchOptions
    >({
        mutationFn: async ({ bucketKey, files, batchSize = 5, onBatchProgress }: BatchOptions) => {
            const total = files.length
            let completed = 0
            const results: FileDTO[] = []

            // dividir en batches
            for (let i = 0; i < files.length; i += batchSize) {
                const batch = files.slice(i, i + batchSize)
                const formData = new FormData()
                formData.append('bucketKey', bucketKey)
                batch.forEach((f: string | Blob) => formData.append('files', f))

                const resp = await api.post<ApiResponse<FileDTO[]>>(
                    '/api/files/upload-many',
                    formData,
                    {
                        headers: { 'Content-Type': 'multipart/form-data' },
                        onUploadProgress: e => {
                            const batchProgress = e.loaded / e.total!
                            const globalProgress = ((i + batchProgress * batch.length) / total)
                            onBatchProgress?.(Math.min(total, Math.round(globalProgress * total)), total)
                        }
                    }
                )
                results.push(...resp.data.data)
                completed += batch.length
                // refresh 
                onBatchProgress?.(completed, total)
            }

            return results
        }
    }
    )
}

// Hook to delete a single file
// Maps to DELETE /api/files/{id}
export function useDeleteFile(): UseMutationResult<
    ApiResponse<null>,
    Error,
    { bucketKey: string; id: number }
> {
    return useMutation<ApiResponse<null>, Error, { bucketKey: string; id: number }>(
        {
            mutationFn: async ({ bucketKey, id }) => {
                const resp = await api.delete<ApiResponse<null>>(
                    `/api/files/${id}`,
                    { headers: { 'bucketKey': bucketKey } }
                )
                return resp.data
            },
        }
    )
}

// Hook to delete many files
// Maps to DELETE /api/files/delete-many
export function useDeleteManyFiles(): UseMutationResult<
    ApiResponse<null>,
    Error,
    { bucketKey: string; ids: number[] }
> {
    return useMutation<ApiResponse<null>, Error, { bucketKey: string; ids: number[] }>(
        {
            mutationFn: async ({ bucketKey, ids }) => {
                const resp = await api.delete<ApiResponse<null>>(
                    '/api/files/delete-many',
                    {
                        data: { bucketKey, ids }
                    }
                );
                return resp.data;
            },
        }
    );
}

// Hook to view a file's metadata and content (as byte array for display)
// Maps to GET /api/files/view/{id}
export function useViewFile(id: number | null, apiKey: string): UseQueryResult<ArrayBuffer, Error> {
    return useQuery<ArrayBuffer, Error>({
        queryKey: ['fileView', id, apiKey],
        queryFn: async () => {
            const resp = await api.get<ArrayBuffer>(
                `/api/files/view/${id}`,
                {
                    headers: { 'X-API-KEY': apiKey },
                    responseType: 'arraybuffer' // for binary data like images/PDFs
                }
            );
            return resp.data;
        },
        // Only run if id and apiKey are provided
        enabled: Boolean(id) && Boolean(apiKey),
        // Cache this query for 5 minutes
        staleTime: 5 * 60 * 1000,
    });
}


// Hook to download a file
// Maps to GET /api/files/download/{id}
export function useDownloadFile(): UseMutationResult<
    Blob,
    Error,
    { id: number; apiKey: string; fileName: string }
> {
    return useMutation<Blob, Error, { id: number; apiKey: string; fileName: string }>(
        {
            mutationFn: async ({ id, apiKey, fileName }) => {
                const resp = await api.get<Blob>(
                    `/api/files/download/${id}`,
                    {
                        headers: { 'X-API-KEY': apiKey },
                        responseType: 'blob'
                    }
                );
                // Create a temporary URL to trigger download
                const url = window.URL.createObjectURL(new Blob([resp.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', fileName);
                document.body.appendChild(link);
                link.click();
                link.remove();
                window.URL.revokeObjectURL(url); // Clean up the URL
                return resp.data;
            },
        }
    );
}

