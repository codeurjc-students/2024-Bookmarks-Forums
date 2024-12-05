# Go to frontend direcroty
cd frontend

# Install Angular CLI
npm install -g @angular/cli

# Node dependencies
npm install

# Switch Angular to production mode (build)
ng build --configuration production

# Go to Docker folder
cd docker

# Docker container build
docker build -t etheko/bookmarks-forums -f Dockerfile ../

# Image push to DockerHub
docker push etheko/bookmarks-forums

# Set up
docker-compose -p bookmarks-forums up
