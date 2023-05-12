package cmu.shaders;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class ShaderProgram {
    int programID;

    int vertexShaderID = -1;
    int fragmentShaderID = -1;
    int geometryShaderID = -1;

    public ShaderProgram() {
        programID = glCreateProgram();
    }

    public void createShader(String source, int shaderType) {
        int shaderID = glCreateShader(shaderType);

        switch (shaderType) {
            case GL_VERTEX_SHADER:
                vertexShaderID = shaderID;
                break;
            case GL_GEOMETRY_SHADER:
                geometryShaderID = shaderID;
                break;
            case GL_FRAGMENT_SHADER:
                fragmentShaderID = shaderID;
                break;
            default:
                throw new NullPointerException("bad");
        }

//        for (int i = 0; i < constNames.length; i++) {
//            String c = constNames[i];
//            float v = constValues[i];
//
//            source.indexOf(c)
//
//        }

        // Compile the shader
        glCompileShader(shaderID);

        // Check for errors
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Error creating shader\n"
                    + glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH)));

        // Attach the shader
        glAttachShader(programID, shaderID);
    }

    public void createVertexShader(String shaderCode) {
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

    }

    public void createFragmentShader(String shaderCode) {
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

    }

    public ShaderProgram createGeometryShader(String shaderCode) {
        // Create the shader and set the source
        geometryShaderID = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShaderID, shaderCode);

        // Compile the shader
        glCompileShader(geometryShaderID);

        // Check for errors
        if (glGetShaderi(geometryShaderID, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Error creating geometry shader\n"
                    + glGetShaderInfoLog(geometryShaderID, glGetShaderi(geometryShaderID, GL_INFO_LOG_LENGTH)));

        // Attach the shader
        glAttachShader(programID, geometryShaderID);

        return this;
    }

    public void link() {
        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Unable to link shader program: " + glGetProgramInfoLog(programID, 1024));
        }
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
        if (vertexShaderID != -1) glDetachShader(programID, vertexShaderID);
        if (geometryShaderID != -1) glDetachShader(programID, geometryShaderID);
        if (fragmentShaderID != -1) glDetachShader(programID, fragmentShaderID);

        // Delete the shaders
        if (vertexShaderID != -1) glDeleteShader(vertexShaderID);
        if (geometryShaderID != -1) glDeleteShader(geometryShaderID);
        if (fragmentShaderID != -1) glDeleteShader(fragmentShaderID);

        // Delete the program
        glDeleteProgram(programID);
    }

    public int getProgramID() {
        return programID;
    }
}
