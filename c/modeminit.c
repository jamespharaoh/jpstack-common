
#include <fcntl.h>
#include <stdio.h>
#include <termios.h>

int main (int argc, char **argv) {
  int fd;
  struct termios settings;
  int result;

  /* Check parms */
  if (argc != 2) {
    fprintf (stderr, "Invalid syntax\n");
    return 1;
  }

  /* Open file */
  fd = open (argv[1], O_RDWR);
  if (fd < 0) {
    perror ("Error opening file");
    return 1;
  }

  /* Get current terminal attributes */
  result = tcgetattr (fd, &settings);
  if (result < 0) {
    perror ("Error in tcgetattr");
    return 1;
  }

  /* Make relevant changes */
  settings.c_iflag &= ~ICRNL;

  /* Update terminal attributes */
  tcsetattr (fd, TCSANOW, &settings);
  if (result < 0) {
    perror ("Error in tcsetattr");
    return 0;
  }

  /* And return */
  return 0;
}
