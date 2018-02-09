//package net.vpc.app.nuts.extensions.repos;
//
//import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
//import org.apache.maven.shared.invoker.*;
//import org.eclipse.aether.DefaultRepositorySystemSession;
//import org.eclipse.aether.RepositorySystem;
//import org.eclipse.aether.artifact.DefaultArtifact;
//import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
//import org.eclipse.aether.impl.DefaultServiceLocator;
//import org.eclipse.aether.repository.LocalRepository;
//import org.eclipse.aether.repository.RemoteRepository;
//import org.eclipse.aether.resolution.*;
//import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
//import org.eclipse.aether.spi.connector.transport.TransporterFactory;
//import org.eclipse.aether.transport.file.FileTransporterFactory;
//import org.eclipse.aether.transport.http.HttpTransporterFactory;
//
//import java.io.File;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Properties;
//
///**
// * Created by vpc on 6/22/17.
// */
//public class MavenAetherExample {
//    public static void main(String[] args) {
//        RepositorySystem repositorySystem = newRepositorySystem();
//        DefaultRepositorySystemSession session = newRepositorySystemSession(repositorySystem);
//        DefaultArtifact artifact = new DefaultArtifact("org.ajax4jsf:ajax4jsf:1.0.5");
//        ArtifactResult artifactResult = null;
//        try {
//            List<RemoteRepository> repositories = Arrays.asList(newCentralRepository());
//            artifactResult = repositorySystem.resolveArtifact(session, new ArtifactRequest(
//                    artifact, repositories,
//                    null
//            ));
//            System.out.println(artifactResult);
//            System.out.println(artifactResult.getArtifact().getFile());
//            ArtifactDescriptorResult artifactDescriptorResult=repositorySystem.readArtifactDescriptor(session,new ArtifactDescriptorRequest(
//                    artifact,repositories,null
//            ));
//            System.out.println(artifactDescriptorResult);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private static RemoteRepository newCentralRepository()
//    {
//        return new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build();
//    }
//
//    public static void mainMavenInvoker(String[] args) {
//        InvocationRequest request = new DefaultInvocationRequest();
//        request.setPomFile( new File( "/path/to/pom.xml" ) );
//        request.setGoals( Collections.singletonList( "dependency:get" ) );
//        request.setProperties(new Properties());
//        request.getProperties().setProperty("remoteRepositories","http://repo1.maven.org/maven2/");
//        request.getProperties().setProperty("groupId","junit");
//        request.getProperties().setProperty("artifactId","junit");
//        request.getProperties().setProperty("version","4.8.2");
//        Invoker invoker = new DefaultInvoker();
//        invoker.setMavenHome(new File("."));
//        try {
//            invoker.execute( request );
//        } catch (MavenInvocationException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public static RepositorySystem newRepositorySystem()
//    {
//        /*
//         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
//         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
//         * factories.
//         */
//        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
//        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
//        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
//        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
//
//        locator.setErrorHandler( new DefaultServiceLocator.ErrorHandler()
//        {
//            @Override
//            public void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
//            {
//                exception.printStackTrace();
//            }
//        } );
//
//        return locator.getService( RepositorySystem.class );
//    }
//
//    public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system )
//    {
//        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
//
//        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
//        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );
//
////        session.setTransferListener( new ConsoleTransferListener() );
////        session.setRepositoryListener( new ConsoleRepositoryListener() );
//
//        // uncomment to generate dirty trees
//        // session.setDependencyGraphTransformer( null );
//
//        return session;
//    }
//}
