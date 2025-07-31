export interface Bucket {
  id: number;
  name: string;
  path: string;
  apiKey: string;
  createdAt: string;
}

export interface FileDTO {
  id: number;
  originalName: string;
  storedName: string;
  relativePath: string;
  mimeType: string;
  sizeBytes: number;
  uploadTime: string;
  hash?: string;
  bucketId?: number;
}

export interface ApiResponse<T = any> {
  status: string;
  code: string;
  message: string;
  description: string | null;
  data: T;
}

export interface LoginResponse {
  token: string;
}
