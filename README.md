# Akka-Http Angular Seed

This project provides the seed for developing an Angular application backed by akka-http.
It is prepared for deployment on Heroku.

## Dependencies

Node and npm

Angular-cli

Sbt

## Install

Clone the repo & delete .git folder

npm install

## Run

sbt "ng build" run

or

ng serve

sbt run

## Setup Intellij Idea (optional)

Add a web module

Run configurations

TsLint

## Configure

Logback.xml & reference.conf

custom configuration: config/application.conf

Backend host and port

## Develop

### Frontend

Angular-cli documentation

Angular documentation

### Backend

net.creasource.http package

net.creasource.core package

net.creasource.web package

Main file

## Stage

sbt compile stage

## Deploy

The Procfile

Deploy on Heroku and configure

## License

Public domain