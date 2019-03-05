const gulp = require('gulp');
const concat = require('gulp-concat');
const rename = require("gulp-rename");

gulp.task('prepare-js', function () {
  return gulp.src(['target/webapp/*.js'])
    .pipe(concat('angular.js'))
    .pipe(gulp.dest('src/main/webapp/dist'));
});

gulp.task('prepare-css', function () {
  return gulp.src(['target/webapp/styles.*.css'])
    .pipe(rename('styles.css'))
    .pipe(gulp.dest('src/main/webapp/dist'));
});

gulp.task('copy-fonts', function () {
  return gulp.src([
    'target/webapp/*',
    '!target/webapp/assets',
    '!target/webapp/*.css',
    '!target/webapp/*.js',
    '!target/webapp/index.html',
  ])
    .pipe(gulp.dest('src/main/webapp/dist'));
});

gulp.task('build', gulp.parallel('prepare-js', 'prepare-css', 'copy-fonts'));
