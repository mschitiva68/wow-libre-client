# Mantener todas las clases con anotaciones de Spring Boot
-keep @org.springframework.boot.autoconfigure.SpringBootApplication class * { *; }
-keep @org.springframework.boot.SpringBootConfiguration class * { *; }
-keep @org.springframework.context.annotation.Bean class * { *; }
-keep @org.springframework.context.annotation.Configuration class * { *; }
-keep @org.springframework.stereotype.Component class * { *; }
-keep @org.springframework.boot.CommandLineRunner class * { *; }
-keep class jakarta.servlet.** { *; }

# Evitar eliminar clases que usan reflexión (Spring, Hibernate, etc.)
-keepnames class * {
    @org.springframework.stereotype.* *;
    @org.springframework.context.annotation.* *;
}

# Mantener entidades JPA (si usas Hibernate)
-keep @jakarta.persistence.Entity class * { *; }
-keep @jakarta.persistence.Embeddable class * { *; }
-keep @jakarta.persistence.MappedSuperclass class * { *; }

# Evitar remover clases que Spring usa en tiempo de ejecución
-keep class org.springframework.** { *; }
-keep class jakarta.** { *; }
-keep class com.fasterxml.jackson.** { *; }

# Evitar warnings innecesarios
-keep class * { *; }
-ignorewarnings
-dontwarn
-dontshrink
-dontoptimize
-dontobfuscate
# Evitar ofuscar y eliminar clases excepto en el paquete duplicado
-keep class ** { *; }
-keep class !duplicated.package.** { *; }

# Mantener las clases de Scoreloop para evitar problemas
-keep class com.scoreloop.** { *; }
