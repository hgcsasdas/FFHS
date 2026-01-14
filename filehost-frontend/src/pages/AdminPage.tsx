import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getUsers, createUser, updateUser, deleteUser } from '../api/usersApi';
import {
  Table,
  Button,
  Modal,
  TextInput,
  Select,
  Group,
  ActionIcon,
  Text,
} from '@mantine/core';
import { useForm } from 'react-hook-form';
import { User } from '../types';
import { Trash, Edit } from 'lucide-react';
import { ToastContainer, toast } from 'react-toastify';

const AdminPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { data: users, isLoading } = useQuery<User[]>({
    queryKey: ['users'],
    queryFn: getUsers,
  });

  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isEditModalOpen, setEditModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<User>();

  const handleCreate = () => {
    reset();
    setCreateModalOpen(true);
  };

  const handleEdit = (user: User) => {
    setSelectedUser(user);
    setValue('username', user.username);
    setValue('role', user.role);
    setEditModalOpen(true);
  };

  const createMutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setCreateModalOpen(false);
      toast.success('User created successfully');
    },
    onError: (error) => {
      toast.error(`Error creating user: ${error.message}`);
    },
  });

  const updateMutation = useMutation({
    mutationFn: updateUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setEditModalOpen(false);
      toast.success('User updated successfully');
    },
    onError: (error) => {
      toast.error(`Error updating user: ${error.message}`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User deleted successfully');
    },
    onError: (error) => {
      toast.error(`Error deleting user: ${error.message}`);
    },
  });

  const onSubmitCreate = (data: User) => {
    createMutation.mutate(data);
  };

  const onSubmitUpdate = (data: User) => {
    if (selectedUser) {
      updateMutation.mutate({ ...data, id: selectedUser.id });
    }
  };

  if (isLoading) return <p>Loading...</p>;

  return (
    <div className="container mx-auto p-4">
      <ToastContainer />
      <Group justify="space-between" mb="md">
        <Text size="xl" fw={700}>User Management</Text>
        <Button onClick={handleCreate}>Create User</Button>
      </Group>

      <Table>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>ID</Table.Th>
            <Table.Th>Username</Table.Th>
            <Table.Th>Role</Table.Th>
            <Table.Th>Actions</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {users?.map((user) => (
            <Table.Tr key={user.id}>
              <Table.Td>{user.id}</Table.Td>
              <Table.Td>{user.username}</Table.Td>
              <Table.Td>{user.role}</Table.Td>
              <Table.Td>
                <Group>
                  <ActionIcon onClick={() => handleEdit(user)}>
                    <Edit size={16} />
                  </ActionIcon>
                  <ActionIcon
                    color="red"
                    onClick={() => {
                      if (window.confirm('Are you sure you want to delete this user?')) {
                        deleteMutation.mutate(user.id);
                      }
                    }}
                  >
                    <Trash size={16} />
                  </ActionIcon>
                </Group>
              </Table.Td>
            </Table.Tr>
          ))}
        </Table.Tbody>
      </Table>

      <Modal
        opened={isCreateModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Create User"
      >
        <form onSubmit={handleSubmit(onSubmitCreate)}>
          <TextInput
            label="Username"
            {...register('username', { required: 'Username is required' })}
            error={errors.username?.message}
          />
          <TextInput
            label="Password"
            type="password"
            {...register('password', { required: 'Password is required' })}
            error={errors.password?.message}
          />
          <Select
            label="Role"
            {...register('role', { required: 'Role is required' })}
            data={['ADMIN', 'USER']}
            error={errors.role?.message}
          />
          <Group justify="flex-end" mt="md">
            <Button type="submit">Create</Button>
          </Group>
        </form>
      </Modal>

      <Modal
        opened={isEditModalOpen}
        onClose={() => setEditModalOpen(false)}
        title="Edit User"
      >
        <form onSubmit={handleSubmit(onSubmitUpdate)}>
          <TextInput
            label="Username"
            {...register('username', { required: 'Username is required' })}
            error={errors.username?.message}
          />
           <TextInput
            label="Password"
            type="password"
            {...register('password')}
            error={errors.password?.message}
          />
          <Select
            label="Role"
            {...register('role', { required: 'Role is required' })}
            data={['ADMIN', 'USER']}
            error={errors.role?.message}
          />
          <Group justify="flex-end" mt="md">
            <Button type="submit">Update</Button>
          </Group>
        </form>
      </Modal>
    </div>
  );
};

export default AdminPage;
