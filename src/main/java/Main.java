import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;

import java.io.IOException;
import java.util.List;


/**
 * Created by moltak on 1/5/17.
 */
public class Main {
    public static void main(String args[]) {
        final String dir = "/home/moltak/mmc/aaa/";
        final String fileanme = "simpson.mp4";
        dockerExecuteUsingSpotifyLibrary(dir, fileanme);
    }

    private static void dockerExecuteUsingSpotifyLibrary(String dir, String filename) {
        // Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
        try {
            final DockerClient docker = DefaultDockerClient.builder().uri("http://localhost:2375").build();
            // List all containers. Only running containers are shown by default.

            final List<Container> containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());
            containers.forEach(i -> System.out.println(i.toString()));

            // config
            final String DOCKER_WORKER_DIR = "/tmp/workdir/"; // 바꾸면 안됨.
            final HostConfig hostConfig
                    = HostConfig.builder().binds(String.format("%s:%s", dir, DOCKER_WORKER_DIR)).build();
            ContainerConfig containerConfig
                    = ContainerConfig.builder().image("jrottenberg/ffmpeg").hostConfig(hostConfig).cmd("-i", DOCKER_WORKER_DIR + filename, "-vf", "fps=1", DOCKER_WORKER_DIR + "out%d.png").build();

            // container
            final ContainerCreation container = docker.createContainer(containerConfig);
            docker.startContainer(container.id());
            final String logs;
            try (LogStream stream = docker.logs(container.id(), DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr())) {
                logs = stream.readFully();
                System.out.println(logs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException | DockerException e) {
            e.printStackTrace();
        }
        System.out.println("--end--");
    }
}
