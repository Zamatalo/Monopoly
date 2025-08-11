# Online Monopoly – Multiplayer Game with Lobby Support

This is a real-time online Monopoly game for up to **4 players** featuring a **3D board**, **lobby system**, and a **microservice-based backend**. The architecture is asynchronous, event-driven, and built using **Redis Streams**, **GraphQL**, and **Webflux**.

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Zamatalo/Monopoly)

[![Docker image](https://img.shields.io/docker/pulls/zamatalo/monopoly-test)](https://hub.docker.com/r/zamatalo/monopoly-test)
## Architecture Overview
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white) 
![GitHub Copilot](https://img.shields.io/badge/github_copilot-8957E5?style=for-the-badge&logo=github-copilot&logoColor=white) 
![Google Assistant](https://img.shields.io/badge/google%20assistant-4285F4?style=for-the-badge&logo=google-assistant&logoColor=white) 
![Google Gemini](https://img.shields.io/badge/google%20gemini-8E75B2?style=for-the-badge&logo=google-gemini&logoColor=white) 
![Perplexity](https://img.shields.io/badge/perplexity-000000?style=for-the-badge&logo=perplexity&logoColor=088F8F)

![Amazon Alexa](https://img.shields.io/badge/amazon%20alexa-52b5f7?style=for-the-badge&logo=amazon%20alexa&logoColor=white) ![Dependabot](https://img.shields.io/badge/dependabot-025E8C?style=for-the-badge&logo=dependabot&logoColor=white) ![DeepSeek](https://img.shields.io/badge/deepseek-000?style=for-the-badge&logo=deepl&logoColor=white) ![Qwen](https://img.shields.io/badge/Qwen-000?style=for-the-badge&logo=openai&logoColor=white) ![Midjourney](https://img.shields.io/badge/midjourney-000?style=for-the-badge&logo=midjourney&logoColor=white)

![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white) ![GitLab CI](https://img.shields.io/badge/gitlab%20ci-%23181717.svg?style=for-the-badge&logo=gitlab&logoColor=white) ![CircleCI](https://img.shields.io/badge/circle%20ci-%23161616.svg?style=for-the-badge&logo=circleci&logoColor=white) ![TravisCI](https://img.shields.io/badge/travis%20ci-%232B2F33.svg?style=for-the-badge&logo=travis&logoColor=white) ![TeamCity](https://img.shields.io/badge/teamcity-000000.svg?style=for-the-badge&logo=teamcity&logoColor=white)

![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white) 
![Dropbox](https://img.shields.io/badge/Dropbox-%233B4D98.svg?style=for-the-badge&logo=Dropbox&logoColor=white) 
![Google Drive](https://img.shields.io/badge/Google%20Drive-4285F4?style=for-the-badge&logo=googledrive&logoColor=white) 
![OneDrive](https://img.shields.io/badge/OneDrive-white?style=for-the-badge&logo=Microsoft%20OneDrive&logoColor=0078D4)
![Mega.nz](https://img.shields.io/badge/Mega-%23D90007.svg?style=for-the-badge&logo=Mega&logoColor=white)

![Bitcoin](https://img.shields.io/badge/bitcoin-2F3134?style=for-the-badge&logo=bitcoin&logoColor=white) 
![Ethereum](https://img.shields.io/badge/ethereum-3C3C3D?style=for-the-badge&logo=ethereum&logoColor=white) 
![Hyperledger](https://img.shields.io/badge/hyperledger-2F3134?style=for-the-badge&logo=hyperledger&logoColor=white)
![Solana](https://img.shields.io/badge/solana-9945FF?style=for-the-badge&logo=solana&logoColor=white) 
![Dogecoin](https://img.shields.io/badge/dogecoin-C2A633?style=for-the-badge&logo=dogecoin&logoColor=white)

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white) ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) ![Spring Data](https://img.shields.io/badge/Spring%20Data-6DB33F?style=for-the-badge&logo=springdata&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)

![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white) ![Django](https://img.shields.io/badge/Django-092E20?style=for-the-badge&logo=django&logoColor=white) ![Flask](https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white) ![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white) ![Pandas](https://img.shields.io/badge/Pandas-150458?style=for-the-badge&logo=pandas&logoColor=white)

![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=node.js&logoColor=white) ![Express](https://img.shields.io/badge/Express.js-000000?style=for-the-badge) ![NestJS](https://img.shields.io/badge/NestJS-E0234E?style=for-the-badge&logo=nestjs&logoColor=white) ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)

![Vue.js](https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vue.js&logoColor=4FC08D) ![Svelte](https://img.shields.io/badge/Svelte-FF3E00?style=for-the-badge&logo=svelte&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black) ![Webpack](https://img.shields.io/badge/Webpack-8DD6F9?style=for-the-badge&logo=webpack&logoColor=black)

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white) ![Helm](https://img.shields.io/badge/Helm-0F1C4D?style=for-the-badge&logo=helm&logoColor=white) ![Terraform](https://img.shields.io/badge/Terraform-623CE4?style=for-the-badge&logo=terraform&logoColor=white) ![Ansible](https://img.shields.io/badge/Ansible-EE0000?style=for-the-badge&logo=ansible&logoColor=white)


![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white) ![CircleCI](https://img.shields.io/badge/CircleCI-343434?style=for-the-badge&logo=circleci&logoColor=white) ![Travis CI](https://img.shields.io/badge/TravisCI-3EAAAF?style=for-the-badge&logo=travis&logoColor=white) ![GitLab](https://img.shields.io/badge/GitLab-FCA121?style=for-the-badge&logo=gitlab&logoColor=white) ![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

![Bitbucket](https://img.shields.io/badge/Bitbucket-0052CC?style=for-the-badge&logo=bitbucket&logoColor=white) ![Azure DevOps](https://img.shields.io/badge/Azure_DevOps-0078D7?style=for-the-badge&logo=azuredevops&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white) ![Google Cloud](https://img.shields.io/badge/Google_Cloud-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white) ![DigitalOcean](https://img.shields.io/badge/DigitalOcean-0080FF?style=for-the-badge&logo=digitalocean&logoColor=white)

![Heroku](https://img.shields.io/badge/Heroku-430098?style=for-the-badge&logo=heroku&logoColor=white) ![Netlify](https://img.shields.io/badge/Netlify-00C7B7?style=for-the-badge&logo=netlify&logoColor=white) ![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white) ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black) ![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white) ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Linux](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black) ![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white) ![Debian](https://img.shields.io/badge/Debian-A81D33?style=for-the-badge&logo=debian&logoColor=white) ![Fedora](https://img.shields.io/badge/Fedora-294172?style=for-the-badge&logo=fedora&logoColor=white) ![Arch Linux](https://img.shields.io/badge/Arch_Linux-1793D1?style=for-the-badge&logo=archlinux&logoColor=white)

![CentOS](https://img.shields.io/badge/CentOS-262577?style=for-the-badge&logo=centos&logoColor=white) ![Red Hat](https://img.shields.io/badge/Red_Hat-EE0000?style=for-the-badge&logo=redhat&logoColor=white) ![Alpine Linux](https://img.shields.io/badge/Alpine_Linux-0B0B0B?style=for-the-badge&logo=alpinelinux&logoColor=white) ![Windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white) ![macOS](https://img.shields.io/badge/macOS-000000?style=for-the-badge&logo=apple&logoColor=white)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![iOS](https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=ios&logoColor=white) ![React Native](https://img.shields.io/badge/React_Native-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![Flutter](https://img.shields.io/badge/Flutter-02569B?style=for-the-badge&logo=flutter&logoColor=white) ![Xamarin](https://img.shields.io/badge/Xamarin-3498DB?style=for-the-badge&logo=xamarin&logoColor=white)

![Unity](https://img.shields.io/badge/Unity-000000?style=for-the-badge&logo=unity&logoColor=white) ![Unreal Engine](https://img.shields.io/badge/Unreal_Engine-0E1128?style=for-the-badge&logo=unrealengine&logoColor=white) ![Blender](https://img.shields.io/badge/Blender-F5792A?style=for-the-badge&logo=blender&logoColor=white) ![Godot](https://img.shields.io/badge/Godot-478CBF?style=for-the-badge&logo=godotengine&logoColor=white) ![Adobe Photoshop](https://img.shields.io/badge/Adobe_Photoshop-31A8FF?style=for-the-badge&logo=adobephotoshop&logoColor=white)

![Adobe Illustrator](https://img.shields.io/badge/Adobe_Illustrator-FF9A00?style=for-the-badge&logo=adobeillustrator&logoColor=white) ![Adobe Premiere Pro](https://img.shields.io/badge/Adobe_Premiere_Pro-9999FF?style=for-the-badge&logo=adobepremierepro&logoColor=white) ![Adobe After Effects](https://img.shields.io/badge/Adobe_After_Effects-9999FF?style=for-the-badge&logo=adobeaftereffects&logoColor=white) ![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white) ![Sketch](https://img.shields.io/badge/Sketch-F7B500?style=for-the-badge&logo=sketch&logoColor=white)

![Jira](https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white) ![Confluence](https://img.shields.io/badge/Confluence-0052CC?style=for-the-badge&logo=confluence&logoColor=white) ![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white) ![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white) ![Trello](https://img.shields.io/badge/Trello-0079BF?style=for-the-badge&logo=trello&logoColor=white)

![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white) ![Google Docs](https://img.shields.io/badge/Google_Docs-4285F4?style=for-the-badge&logo=googledocs&logoColor=white) ![Google Sheets](https://img.shields.io/badge/Google_Sheets-0F9D58?style=for-the-badge&logo=googlesheets&logoColor=white) ![Google Slides](https://img.shields.io/badge/Google_Slides-F4B400?style=for-the-badge&logo=googleslides&logoColor=white) ![Microsoft Office](https://img.shields.io/badge/Microsoft_Office-D83B01?style=for-the-badge&logo=microsoftoffice&logoColor=white)

![Visual Studio Code](https://img.shields.io/badge/VS_Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white) ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white) ![PyCharm](https://img.shields.io/badge/PyCharm-000000?style=for-the-badge&logo=pycharm&logoColor=white) ![Eclipse](https://img.shields.io/badge/Eclipse-2C2255?style=for-the-badge&logo=eclipseide&logoColor=white) ![NetBeans](https://img.shields.io/badge/NetBeans-2D2D2D?style=for-the-badge&logo=netbeans&logoColor=white)

![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white) ![Telegram](https://img.shields.io/badge/Telegram-0088CC?style=for-the-badge&logo=telegram&logoColor=white) ![WhatsApp](https://img.shields.io/badge/WhatsApp-25D366?style=for-the-badge&logo=whatsapp&logoColor=white) ![Twitter](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white) ![Facebook](https://img.shields.io/badge/Facebook-1877F2?style=for-the-badge&logo=facebook&logoColor=white)
![Instagram](https://img.shields.io/badge/Instagram-E4405F?style=for-the-badge&logo=instagram&logoColor=white) ![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white) ![YouTube](https://img.shields.io/badge/YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white) ![Pinterest](https://img.shields.io/badge/Pinterest-E60023?style=for-the-badge&logo=pinterest&logoColor=white) ![Snapchat](https://img.shields.io/badge/Snapchat-FFFC00?style=for-the-badge&logo=snapchat&logoColor=black)

![TikTok](https://img.shields.io/badge/TikTok-000000?style=for-the-badge&logo=tiktok&logoColor=white) ![Reddit](https://img.shields.io/badge/Reddit-FF4500?style=for-the-badge&logo=reddit&logoColor=white) ![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white) ![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white) ![GitLab](https://img.shields.io/badge/GitLab-FCA121?style=for-the-badge&logo=gitlab&logoColor=white)

![Stack Overflow](https://img.shields.io/badge/Stack_Overflow-FE7A16?style=for-the-badge&logo=stackoverflow&logoColor=white) ![HackerRank](https://img.shields.io/badge/HackerRank-2EC866?style=for-the-badge&logo=hackerrank&logoColor=white) ![LeetCode](https://img.shields.io/badge/LeetCode-FFA116?style=for-the-badge&logo=leetcode&logoColor=white) ![CodePen](https://img.shields.io/badge/CodePen-000000?style=for-the-badge&logo=codepen&logoColor=white) ![JSFiddle](https://img.shields.io/badge/JSFiddle-4675A6?style=for-the-badge&logo=jsfiddle&logoColor=white)

![CodeSandbox](https://img.shields.io/badge/CodeSandbox-000000?style=for-the-badge&logo=codesandbox&logoColor=white) ![Visual Studio](https://img.shields.io/badge/Visual_Studio-5C2D91?style=for-the-badge&logo=visualstudio&logoColor=white) ![JetBrains](https://img.shields.io/badge/JetBrains-000000?style=for-the-badge&logo=jetbrains&logoColor=white) ![AWS Lambda](https://img.shields.io/badge/AWS_Lambda-FF9900?style=for-the-badge&logo=aws-lambda&logoColor=white) ![Azure Functions](https://img.shields.io/badge/Azure_Functions-0078D7?style=for-the-badge&logo=azurefunctions&logoColor=white)

![Google Analytics](https://img.shields.io/badge/Google_Analytics-F9A825?style=for-the-badge&logo=googleanalytics&logoColor=white) ![Google Ads](https://img.shields.io/badge/Google_Ads-0F9D58?style=for-the-badge&logo=googleads&logoColor=white) ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black) ![Sentry](https://img.shields.io/badge/Sentry-362D59?style=for-the-badge&logo=sentry&logoColor=white) ![New Relic](https://img.shields.io/badge/New_Relic-0089D0?style=for-the-badge&logo=newrelic&logoColor=white)

![Datadog](https://img.shields.io/badge/Datadog-632CA6?style=for-the-badge&logo=datadog&logoColor=white) ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white) ![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white) ![Kibana](https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white) ![PagerDuty](https://img.shields.io/badge/PagerDuty-0052CC?style=for-the-badge&logo=pagerduty&logoColor=white)

![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white) ![Zoom](https://img.shields.io/badge/Zoom-2D8CFF?style=for-the-badge&logo=zoom&logoColor=white) ![Google Meet](https://img.shields.io/badge/Google_Meet-34A853?style=for-the-badge&logo=googlemeet&logoColor=white) ![Microsoft Teams](https://img.shields.io/badge/Microsoft_Teams-6264A7?style=for-the-badge&logo=microsoftteams&logoColor=white) ![Skype](https://img.shields.io/badge/Skype-00AFF0?style=for-the-badge&logo=skype&logoColor=white)

![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white) ![Insomnia](https://img.shields.io/badge/Insomnia-4000BF?style=for-the-badge&logo=insomnia&logoColor=white) ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) ![OpenAPI](https://img.shields.io/badge/OpenAPI-6AD8FF?style=for-the-badge&logo=openapiinitiative&logoColor=black) ![GraphQL](https://img.shields.io/badge/GraphQL-E10098?style=for-the-badge&logo=graphql&logoColor=white)

![REST API](https://img.shields.io/badge/REST_API-61DAFB?style=for-the-badge) ![gRPC](https://img.shields.io/badge/gRPC-339933?style=for-the-badge&logo=grpc&logoColor=white) ![OAuth](https://img.shields.io/badge/OAuth-4285F4?style=for-the-badge&logo=oauth&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white) ![OpenID](https://img.shields.io/badge/OpenID-000000?style=for-the-badge&logo=openid&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white) ![Helm](https://img.shields.io/badge/Helm-0F1C4D?style=for-the-badge&logo=helm&logoColor=white) ![Terraform](https://img.shields.io/badge/Terraform-5D4F85?style=for-the-badge&logo=terraform&logoColor=white) ![Ansible](https://img.shields.io/badge/Ansible-EE0000?style=for-the-badge&logo=ansible&logoColor=white)

![Puppet](https://img.shields.io/badge/Puppet-FFAE00?style=for-the-badge&logo=puppet&logoColor=black) ![Chef](https://img.shields.io/badge/Chef-8D0000?style=for-the-badge&logo=chef&logoColor=white) ![SaltStack](https://img.shields.io/badge/SaltStack-193549?style=for-the-badge&logo=saltstack&logoColor=white) ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

![CircleCI](https://img.shields.io/badge/CircleCI-343434?style=for-the-badge&logo=circleci&logoColor=white) ![Travis CI](https://img.shields.io/badge/TravisCI-3EAAAF?style=for-the-badge&logo=travis&logoColor=white) ![Bitbucket Pipelines](https://img.shields.io/badge/Bitbucket_Pipelines-0052CC?style=for-the-badge&logo=bitbucket&logoColor=white) ![Azure Pipelines](https://img.shields.io/badge/Azure_Pipelines-0078D7?style=for-the-badge&logo=azuredevops&logoColor=white) ![GitLab CI](https://img.shields.io/badge/GitLab_CI-FCA121?style=for-the-badge&logo=gitlab&logoColor=white)

![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white) ![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white) ![ELK Stack](https://img.shields.io/badge/ELK_Stack-005571?style=for-the-badge&logo=elastic&logoColor=white) ![Nagios](https://img.shields.io/badge/Nagios-333333?style=for-the-badge&logo=nagios&logoColor=white) ![Zabbix](https://img.shields.io/badge/Zabbix-FFFFFF?style=for-the-badge&logo=zabbix&logoColor=red)

![Selenium](https://img.shields.io/badge/Selenium-43B02A?style=for-the-badge&logo=selenium&logoColor=white) ![Cypress](https://img.shields.io/badge/Cypress-17202C?style=for-the-badge&logo=cypress&logoColor=white) ![Jest](https://img.shields.io/badge/Jest-C21325?style=for-the-badge&logo=jest&logoColor=white) ![Mocha](https://img.shields.io/badge/Mocha-8D6748?style=for-the-badge&logo=mocha&logoColor=white) ![JUnit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=junit5&logoColor=white)

![TestCafe](https://img.shields.io/badge/TestCafe-0052CC?style=for-the-badge&logo=testcafe&logoColor=white) ![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white) ![Insomnia](https://img.shields.io/badge/Insomnia-4000BF?style=for-the-badge&logo=insomnia&logoColor=white) ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) ![OpenAPI](https://img.shields.io/badge/OpenAPI-6AD8FF?style=for-the-badge&logo=openapiinitiative&logoColor=black)

![GraphQL](https://img.shields.io/badge/GraphQL-E10098?style=for-the-badge&logo=graphql&logoColor=white) ![REST API](https://img.shields.io/badge/REST_API-61DAFB?style=for-the-badge) ![OAuth](https://img.shields.io/badge/OAuth-4285F4?style=for-the-badge&logo=oauth&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white) ![OpenID](https://img.shields.io/badge/OpenID-000000?style=for-the-badge&logo=openid&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white) ![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![C#](https://img.shields.io/badge/C%23-239120?style=for-the-badge&logo=c-sharp&logoColor=white)

![Go](https://img.shields.io/badge/Go-00ADD8?style=for-the-badge&logo=go&logoColor=white) ![Rust](https://img.shields.io/badge/Rust-000000?style=for-the-badge&logo=rust&logoColor=white) ![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white) ![Swift](https://img.shields.io/badge/Swift-F05138?style=for-the-badge&logo=swift&logoColor=white) ![PHP](https://img.shields.io/badge/PHP-777BB4?style=for-the-badge&logo=php&logoColor=white)

![Ruby](https://img.shields.io/badge/Ruby-CC342D?style=for-the-badge&logo=ruby&logoColor=white) ![Scala](https://img.shields.io/badge/Scala-DC322F?style=for-the-badge&logo=scala&logoColor=white) ![Dart](https://img.shields.io/badge/Dart-0175C2?style=for-the-badge&logo=dart&logoColor=white) ![Elixir](https://img.shields.io/badge/Elixir-4B275F?style=for-the-badge&logo=elixir&logoColor=white) ![Haskell](https://img.shields.io/badge/Haskell-5D4F85?style=for-the-badge&logo=haskell&logoColor=white)

![C++](https://img.shields.io/badge/C++-00599C?style=for-the-badge&logo=c%2B%2B&logoColor=white) ![C](https://img.shields.io/badge/C-555555?style=for-the-badge&logo=c&logoColor=white) ![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white) ![Sass](https://img.shields.io/badge/Sass-CC6699?style=for-the-badge&logo=sass&logoColor=white)

![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwind-css&logoColor=white) ![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black) ![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white) ![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)

![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=node.js&logoColor=white) ![Express](https://img.shields.io/badge/Express-000000?style=for-the-badge&logo=express&logoColor=white) ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Django](https://img.shields.io/badge/Django-092E20?style=for-the-badge&logo=django&logoColor=white) ![Flask](https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white)

The system is split into **six main services**:

---

### 1.  API Gateway

- Accepts all **GraphQL** client requests.
- Stores **`CompletableFuture`** objects associated with a **Correlation ID**.
- Publishes these requests into a **Redis Streams** channel.
- Waits for a corresponding response from the backend.
- Listens for **gameUpdate** and **diceUpdate** redis channels, sends the received data via GraphQL subscriptions(Websockets) to clients
---

### 2.  API Logic

- Subscribes to the Redis Streams channel.
- Listens for incoming game actions.
- Validates the game state and computes possible actions.
- Handles logic(dice roll request, bot's turns)
- Sends results back to the API Gateway using the correlation ID via redis.

---

### 3.  Frontend

- Uses **two WebSocket connections via apollo client** :
  - One for game state updates
  - One for dice updates
- Renders a **3D Monopoly board** and players using **Three.js**.
- Animates player movement with **GSAP**.
- Handles UI logic with **React** components.
---

### 4.  Dice Server

- Listens for dice roll requests via Redis.
- Simulates a roll for each individual game using Rapier3d (ensuring a **single source of truth**).
- Publishes roll results back to Redis for consumption by the API Logic.
- Published **Dice position and rotation** to Api-gateway
---
### 5. Persistence Module

### 6.  Shared Module

- Common config and service for redis. Shared Exceptions between API-Gateway and API-Logic

--- 

### 7.  GraphQL Module

- Centralized GraphQL schema and resolvers.
- Uses **graphqlcodegen-maven-plugin** for generating **DTO's,enums,query's**...
- Shared between Api-gateway, Api-logic, api-Persistence.

---

## ⚙️ Tech Stack

| Layer         | Technologies                                                                    |
|---------------|---------------------------------------------------------------------------------|
| Frontend      | React, Three.js, GSAP(for animations), Apollo-client, Node                      |
| Backend       | Spring-boot, WebFlux (Mono/Flux)                                                |
| Communication | Redis (Streams + Pub/Sub), CompletableFuture                                    |
| Architecture  | Microservices, Event-driven                                                     |
| Deployment    | Docker-files and docker-compose,Nginx (static frontend + reverse proxy for API) |
---

## 📦 Installation & Run
- Everything is packed into docker-compose.yml file you just need to copy it and pull the needed images.
---

```bash
  
docker-compose pull
docker-compose up
```
The server will run on http://localhost:80