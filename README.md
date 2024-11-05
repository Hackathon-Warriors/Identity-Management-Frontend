# Identity Verification Android App (2023)

Water Budget Application is redesigned in Android Framwork where we have used Kotlin language for developement and used MVVM design pattern and following the SOLID principles for archtecture design where we can add and remove things easily.


# Application Technical Details 

**Package Structure** : As we are following MVVM design pattern so we have below main packages
1. **Modules** - In modules package we have created all the features under its subpackage like cropplan, dashboard, login, waterconsumption etc, Under these sub packages we have ui packege for UI layer and viewmodel packege for feature specific view models. So in future if someone wants to add new feature then they can add new package under modules package by following the same pattern.

2. **Network** - Network package is basically responsible for all the network calls, We have created one common network interface for whole application that is INetworkHelper which extends feature specific network interfaces like IUserVerification, ISources etc. So the approach is whenever we want to add any new network call or multiple network calls for one feature we should create its own network interface and extend it in INetworkHelper. We have designed network interface in such a way where we can use any network client like retrofit, volley or our own custom network library. Currently we have used retrofit which actually provides the implementation of INetworkHelper.

3. **Repository** - Respository package we have designed for providing data from data sources that can be network api call or moblie client database. Currently we are taking all the data from network calls. After taking data from datasource repository pass it to view modles and further view modles apply some business logic and pass the filtered data to UI layer.

4. **DI** - DI package is desined for dependency injection and we have used dagger hilt library for this.

5. **Utils** - Utils package containes different type of utility which are used in whole application.


More details about the application flows and feature details please [refer](https://gitlab.wotr.org.in/water-budget-app-dev/water-budget-app-documentation/-/wikis/home)
