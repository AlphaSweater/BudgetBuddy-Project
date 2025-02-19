name: CI

on:
  push:
    branches:
      - main
      - development
      - feature/*
      - bugfix/*
  pull_request:
    branches:
      - main
      - development
      - feature/*
      - bugfix/*

jobs:
  build:
    runs-on: windows-latest  # Change this to run the workflow on Windows

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up .NET
        uses: actions/setup-dotnet@v3
        with:
          dotnet-version: '8.x'  # Adjust the .NET version as needed

      - name: Restore dependencies
        run: dotnet restore

      - name: Build
        run: dotnet build --no-restore

      - name: Test
        run: dotnet test --no-build --verbosity normal
        
