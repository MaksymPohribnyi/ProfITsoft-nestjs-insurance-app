FROM node:18-alpine as production-dependencies
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --production

FROM node:18-alpine as build-dependencies
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:18-alpine as builder
WORKDIR /app
COPY --from=build-dependencies /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
COPY --from=production-dependencies /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY package.json ./

CMD ["node", "dist/main.js"]