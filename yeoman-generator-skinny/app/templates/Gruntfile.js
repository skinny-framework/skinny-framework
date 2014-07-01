module.exports = function (grunt) {
    require('load-grunt-tasks')(grunt);
 
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        typescript: {
            base: {
                src: ['src/main/webapp/assets/grunt/ts/**/*.ts'],
                dest: 'src/main/webapp/assets/js/ts-all.js',
                options: {
                    module: 'amd',
                    target: 'es5',
                    sourceMap: true
                }
            }
        },
        coffee: {
            compileWithMaps: {
                options: {
                    sourceMap: true
                },
                files: {
                    'src/main/webapp/assets/js/coffee-all.js': ['src/main/webapp/assets/grunt/coffee/**/*.coffee']
                }
            }
        },
        react: {
            combined_file_output: {
                files: {
                    'src/main/webapp/assets/js/react-all.js': ['src/main/webapp/assets/grunt/jsx/**/*.jsx']
                }
            }
        },
        less: {
            development: {
                options: {
                    sourceMap: false
                },
                files: {
                    "src/main/webapp/assets/css/application-less.css": ["src/main/webapp/assets/grunt/less/application.less"]
                }
            }
        },
        sass: { 
            dist: {
                options: {
                    sourcemap: true
                },
                files: {
                    'src/main/webapp/assets/css/application-scss.css': ['src/main/webapp/assets/grunt/scss/application.scss']
                }
            }
        },
        concat: {
            options: {
                separator: ';',
            },
            dist: {
                    src: [
                        'src/main/webapp/assets/js/coffee-all.js',
                        'src/main/webapp/assets/js/react-all.js',
                        'src/main/webapp/assets/js/ts-all.js',
                    ],
                    dest: 'src/main/webapp/assets/js/application-all.js',
            },
        },
        watch: {
            files: 'src/main/webapp/assets/grunt/**/*',
            tasks: ['typescript', 'coffee', 'react', 'sass', 'less']
        },
        mochaTest: {
            test: {
                options: {
                    reporter: 'spec'
                },
                src: ['test/**/*.js']
            }
        },
        mocha_phantomjs: {
            all: ['src/test/phantomjs/index.html']
        },
        uglify: {
            my_target: {
                files: {
                    'src/main/webapp/assets/js/application-all.min.js': ['src/main/webapp/assets/js/application-all.js']
                }
            }
        }
    });

    grunt.registerTask('compile', ['typescript', 'coffee',  'react', 'sass', 'less', 'concat']);
    grunt.registerTask('phantomJsTest', ['connect', 'mocha_phantomjs']);
    grunt.registerTask('test',    ['compile', 'mochaTest', 'phantomJsTest']);
    grunt.registerTask('build',   ['compile', 'mochaTest', 'uglify']);
    grunt.registerTask('default', ['compile', 'uglify', 'watch']);
 
}

