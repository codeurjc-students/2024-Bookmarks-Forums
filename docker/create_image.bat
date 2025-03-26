REM Remove specific files in backend static folder
del ..\backend\src\main\resources\static\index.html
del ..\backend\src\main\resources\static\main-*.js
del ..\backend\src\main\resources\static\polyfills-*.js
del ..\backend\src\main\resources\static\scripts-*.js
del ..\backend\src\main\resources\static\styles-*.js

REM Go to frontend directory
cd ..\frontend

REM Install Angular CLI
npm install -g @angular/cli

REM Node dependencies
npm install

REM Switch Angular to production mode (build)
ng build --configuration production

REM Copy Angular build to backend static folder
xcopy dist\frontend\browser\* ..\backend\src\main\resources\static\ /E /I /Y

REM Go to backend directory
cd ..\backend

REM Maven build
.\mvnw.cmd clean package -DskipTests -P local

REM Go to Docker folder
cd ..\docker

REM Docker container build
docker build -t etheko/bookmarks-forums -f Dockerfile ..

REM Image push to DockerHub
docker push etheko/bookmarks-forums

REM Set up
docker-compose -p bookmarks-forums up --build
