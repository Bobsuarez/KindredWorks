import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import apiClient from '../services/apiClient';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null);
  const isAuthenticated = ref<boolean>(false);

  const isLoggedIn = computed(() => isAuthenticated.value);

  const initAuth = () => {
    if (typeof window !== 'undefined') {
      const savedToken = localStorage.getItem('auth_token');
      if (savedToken) {
        token.value = savedToken;
        isAuthenticated.value = true;
      }
    }
  };

  const login = async (credentials: { username: string; password: string }) => {
    try {
      const response = await apiClient.post('/api/auth/login', credentials);
      const { token: receivedToken } = response.data;

      token.value = receivedToken;
      isAuthenticated.value = true;

      if (typeof window !== 'undefined') {
        localStorage.setItem('auth_token', receivedToken);
      }

      return { success: true };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Error al iniciar sesión. Verifica tus credenciales.'
      };
    }
  };

  const logout = () => {
    token.value = null;
    isAuthenticated.value = false;

    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
  };

  initAuth();

  return {
    token,
    isAuthenticated,
    isLoggedIn,
    login,
    logout,
    initAuth,
  };
});
