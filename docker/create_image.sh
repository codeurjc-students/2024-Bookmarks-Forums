# Remove specific files in backend static folder
rm -f backend/src/main/resources/static/index.html
rm -f backend/src/main/resources/static/main-*.js
rm -f backend/src/main/resources/static/polyfills-*.js
rm -f backend/src/main/resources/static/scripts-*.js
rm -f backend/src/main/resources/static/styles-*.js

# Go to frontend direcroty
cd frontend

# Install Angular CLI
npm install -g @angular/cli

# Node dependencies
npm install

# Switch Angular to production mode (build)
ng build --configuration production

# Copy Angular build to backend static folder
cp -r dist/frontend/browser/* ../backend/src/main/resources/static/

# Go to backend directory
cd ../backend

# Maven build
./mvnw clean package -DskipTests -P local

# Go to Docker folder
cd ../docker

# Docker container build
docker build -t etheko/bookmarks-forums -f Dockerfile ../

# Image push to DockerHub
docker push etheko/bookmarks-forums

# Set up
docker-compose -p bookmarks-forums up --build
