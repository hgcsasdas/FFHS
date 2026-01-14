import { api } from './client';
import { User } from '../types';

export const getUsers = async (): Promise<User[]> => {
  const { data } = await api.get('/users');
  return data;
};

export const createUser = async (user: User): Promise<User> => {
  const { data } = await api.post('/users/create', user);
  return data;
};

export const updateUser = async (user: User): Promise<User> => {
  const { data } = await api.put(`/users/${user.id}`, user);
  return data;
};

export const deleteUser = async (userId: string): Promise<void> => {
  await api.delete(`/users/${userId}`);
};
