FROM node:18-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build


FROM node:18-alpine

WORKDIR /app

# Copy only build output
COPY --from=build /app/dist ./dist

# Install only production dependencies
COPY package.json package-lock.json ./
RUN npm install 

EXPOSE 3000

CMD ["npm", "run", "preview", "--", "--host"]

