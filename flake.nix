{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
    systems.url = "github:nix-systems/default";
  };

  outputs = inputs:
    inputs.flake-parts.lib.mkFlake { inherit inputs; } {
      systems = import inputs.systems;

      perSystem = { config, self', pkgs, lib, system, ... }: let
        java = pkgs.jetbrains.jdk-no-jcef;

        nativeBuildInputs = with pkgs; [
          java
          git
        ];

        buildInputs = with pkgs; [
          libGL
          glfw
          xorg.libX11
          xorg.libXext
          xorg.libXcursor
          xorg.libXrandr
          xorg.libXxf86vm
          xorg.xrandr
          glfw-wayland-minecraft # Not always needed, but in case it is, it's here.
          flite # TTS
          libpulseaudio # Required for audio
        ];
      in {
        devShells.default = pkgs.mkShell {
          inherit nativeBuildInputs buildInputs;

          env = {
            LD_LIBRARY_PATH = lib.makeLibraryPath buildInputs;
            JAVA_HOME = "${java.home}";
          };
        };
      };
    };
}