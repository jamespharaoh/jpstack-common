/*
 * socktest.c
 *
 * james@pharaohsystems.com
 *
 * 9 September 2005
 *
 * Attempts to open a TCP socket to the given host and port number. The exit code indicates success
 * or otherwise. Intended for use in scripts to check whether a service is running.
 */

// ============================================================ includes

#include <errno.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

// ============================================================ exit status codes

const int er_success = 0;
const int er_connect_refused = 1;
const int er_connect_other = 2;
const int er_syntax = 10;
const int er_hostname = 20;
const int er_other = 99;

// ============================================================ syntax hint message

const char *syntax = "Syntax: socktest <hostname> <port>\n";

// ============================================================ main()

int main (int argc, char **argv) {

  // check syntax
  if (argc != 3) {
    fprintf (stderr, syntax);
    return er_syntax;
  }

  // get hostname
  char *hostname = argv [1];
  struct hostent *hostent = gethostbyname2 (hostname, AF_INET);
  if (! hostent) {
    fprintf (stderr, "Host not found\n");
    return er_hostname;
  }

  // get port
  char *portstr = argv [2];
  char *tail;
  errno = 0;
  int portnum = strtol (portstr, &tail, 10);
  if (errno) {
    fprintf (stderr, syntax);
    return er_syntax;
  }
  if (*tail) {
    fprintf (stderr, syntax);
    return er_syntax;
  }

  // create the socket
  int sock = socket (PF_INET, SOCK_STREAM, IPPROTO_TCP);
  if (sock < 0) {
    fprintf (stderr, "Error creating socket\n");
    return er_other;
  }

  // setup the connect address
  struct sockaddr_in servaddr;
  memset (&servaddr, 0, sizeof (servaddr));
  servaddr.sin_family = AF_INET;
  memcpy (&servaddr.sin_addr.s_addr, hostent->h_addr, hostent->h_length);
  servaddr.sin_port = htons (portnum);

  // connect the socket
  if (connect (sock, (sockaddr*) &servaddr, sizeof (servaddr)) < 0) {
    return errno == ECONNREFUSED? er_connect_refused : er_connect_other;
  }

  // close and exit
  close (sock);
  return er_success;
}
