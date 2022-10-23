const vec3 data[2] = vec3[2](
vec3(0.2, 0.5, 0.005),
vec3(0.6, 0.4, 0.015)
);

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;

    float w = 0.0;
    for (int i = 0; i < data.length(); i++) {
        vec3 v = data[i];
        v.x += (sin(iTime) * 0.1) * float(i);
        v.y += (cos(iTime) * 0.1);
        
        float l = length(uv - v.xy);
        w +=  v.z / (l * l);
    }
    
    float m = 1.0 - clamp(abs(w - 0.5) * 25.0, 0.0, 1.0);
    m += sin(m) * 0.5;

    // Output to screen
    fragColor = vec4(m);
}