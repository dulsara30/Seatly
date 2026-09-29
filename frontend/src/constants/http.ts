/** The HTTP statuses the app reacts to, named once for client and server code alike. */
export const HttpStatus = {
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  /** 500 and above: the server failed, not the request. */
  SERVER_ERROR_FLOOR: 500,
  BAD_GATEWAY: 502,
} as const;
