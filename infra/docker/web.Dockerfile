# syntax=docker/dockerfile:1

FROM node:22-alpine AS builder

WORKDIR /workspace

COPY web/package.json web/package-lock.json ./

RUN --mount=type=cache,target=/root/.npm \
    npm ci

COPY web/ ./

RUN npm run build


FROM nginx:1.29-alpine AS runtime

RUN rm -rf /usr/share/nginx/html/*

COPY infra/docker/nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder \
    /workspace/dist/barkly/browser/ \
    /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]