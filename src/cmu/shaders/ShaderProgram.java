package cmu.shaders;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class ShaderProgram {
    int programID;

    int vertexShaderID;
    int fragmentShaderID;
    int geometryShaderID;

    public ShaderProgram() {
        programID = glCreateProgram();
    }

    public ShaderProgram createVertexShader(String shaderCode) {
        // Create the shader and set the source
        vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderID, shaderCode);

        // Compile the shader
        glCompileShader(vertexShaderID);

        // Check for errors
        if (glGetShaderi(vertexShaderID, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Error creating vertex shader\n"
                    + glGetShaderInfoLog(vertexShaderID, glGetShaderi(vertexShaderID, GL_INFO_LOG_LENGTH)));

        // Attach the shader
        glAttachShader(programID, vertexShaderID);

        return this;
    }

    public ShaderProgram createFragmentShader(String shaderCode) {
        // Create the shader and set the source
        fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderID, shaderCode);

        // Compile the shader
        glCompileShader(fragmentShaderID);

        // Check for errors
        if (glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Error creating fragment shader\n"
                    + glGetShaderInfoLog(fragmentShaderID, glGetShaderi(fragmentShaderID, GL_INFO_LOG_LENGTH)));

        // Attach the shader
        glAttachShader(programID, fragmentShaderID);

        return this;
    }

    public ShaderProgram createGeometryShader(String shaderCode) {
        // Create the shader and set the source
        geometryShaderID = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShaderID, shaderCode);

        // Compile the shader
        glCompileShader(geometryShaderID);

        // Check for errors
        if (glGetShaderi(geometryShaderID, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Error creating fragment shader\n"
                    + glGetShaderInfoLog(geometryShaderID, glGetShaderi(geometryShaderID, GL_INFO_LOG_LENGTH)));

        // Attach the shader
        glAttachShader(programID, geometryShaderID);

        return this;
    }

    public ShaderProgram link() {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Unable to link shader program: " + glGetProgramInfoLog(programID, 1024));
        }

        return this;
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void dispose() {
        // Unbind the program
        unbind();

        // Detach the shaders
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);

        // Delete the shaders
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);

        // Delete the program
        glDeleteProgram(programID);
    }

    public int getProgramID() {
        return programID;
    }
}
