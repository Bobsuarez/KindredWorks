import { defineMiddleware } from 'astro:middleware';

export const onRequest = defineMiddleware((context, next) => {
  const { pathname } = context.url;
  const isAppRoute = pathname.startsWith('/app');

  /*if (isAppRoute) {
    const authToken = context.cookies.get('auth_token')?.value;

    if (!authToken) {
      return context.redirect('/login');
    }
  }*/

  return next();
});
