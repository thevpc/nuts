---
id: app-xspring-cli
title: Spring Cli
sidebar_label: Spring Cli
order: 2
---

${{include($"${resources}/header.md")}}

## T0014- Spring Cli (Spring Boot Client App)
```
nuts install org.springframework.boot:spring-boot-cli
nuts settings add alias spring="--main-class=1 spring-boot-cli"
# Examples of usage
nuts spring --version
nuts spring init --dependencies=web,data-jpa my-project
``` 
