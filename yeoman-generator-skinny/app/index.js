'use strict';
var util = require('util');
var path = require('path');
var yeoman = require('yeoman-generator');

var SkinnyGenerator = module.exports = function SkinnyGenerator(args, options, config) {
  yeoman.generators.Base.apply(this, arguments);
  this.on('end', function () {
    this.installDependencies({ skipInstall: options['skip-install'] });
  });
  this.pkg = JSON.parse(this.readFileAsString(path.join(__dirname, '../package.json')));
};

util.inherits(SkinnyGenerator, yeoman.generators.Base);

SkinnyGenerator.prototype.app = function app() {
  this.directory('src', 'src');
  this.directory('bin', 'bin');
  this.directory('task', 'task');
  this.directory('project', 'project');
  this.copy('_gitignore', '.gitignore');
  this.copy('build.sbt', 'build.sbt');
  this.copy('package.json', 'package.json');
  this.copy('README.md', 'README.md');
  this.copy('skinny', 'skinny');
  this.copy('skinny.bat', 'skinny.bat');
  this.copy('sbt.bat', 'sbt.bat');
};

