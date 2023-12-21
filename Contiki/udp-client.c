#include "contiki.h"
#include "net/routing/routing.h"
#include "random.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"
#include <math.h>

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define WITH_SERVER_REPLY  1
#define UDP_CLIENT_PORT	8765
#define UDP_SERVER_PORT	5678

static struct simple_udp_connection udp_conn;

#define MAX_READINGS 10
#define SEND_INTERVAL (20 * CLOCK_SECOND)
#define FAKE_TEMPS 5

static struct simple_udp_connection udp_conn;
static float readings[MAX_READINGS];
static unsigned next_reading=0;


/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);
/*---------------------------------------------------------------------------*/
static unsigned
get_temperature()
{
  static unsigned fake_temps [FAKE_TEMPS] = {30, 25, 20, 15, 10};
  return fake_temps[random_rand() % FAKE_TEMPS];
}

static int is_empty()
{
  for (size_t i = 0; i < MAX_READINGS; ++i) {
        if (readings[i] != 0) {
            return 0;  // Array is not empty
        }
    }
    return 1; //is empty
}

static float get_average()
{
  float average;
  unsigned sum = 0;
  unsigned no = 0;

  static uint8_t i=0;
  for (i=0; i<MAX_READINGS; i++) {
    if (readings[i]!=0){
      sum = sum+readings[i];
      no++;
    }
  }
  average = ((float)sum)/no;
  return average;
}

static void clear_readings()
{
  for (size_t i = 0; i < MAX_READINGS; ++i) {
        readings[i] = 0;
    }
}
/*---------------------------------------------------------------------------*/
static void
udp_rx_callback(struct simple_udp_connection *c,
         const uip_ipaddr_t *sender_addr,
         uint16_t sender_port,
         const uip_ipaddr_t *receiver_addr,
         uint16_t receiver_port,
         const uint8_t *data,
         uint16_t datalen)
{
  // ...
  LOG_INFO("Someone send a message to this client: ");
  LOG_INFO_("\n");
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data)
{
  static struct etimer periodic_timer;
  uip_ipaddr_t dest_ipaddr;
  static float temperature;
  static float average;

  PROCESS_BEGIN();

  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                      UDP_SERVER_PORT, udp_rx_callback);

  //...
  etimer_set(&periodic_timer, random_rand() % SEND_INTERVAL);
  while(1) {
  PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));
  
  if(NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
    //connected to root

    //check if values saved
    if(is_empty() == 1)
    {
      //it's empty
      temperature = (float)get_temperature();
      LOG_INFO("Sending temperature from sensor: %f to ", temperature);
      LOG_INFO_6ADDR(&dest_ipaddr);
      LOG_INFO_("\n");
      simple_udp_sendto(&udp_conn, &temperature, sizeof(temperature), &dest_ipaddr);
    }else{
      //compute average
      average = get_average();
      clear_readings();
      
      //int int_average = round(average);
      simple_udp_sendto(&udp_conn, &average, sizeof(average), &dest_ipaddr);
      LOG_INFO("Sending average temperature: %f to ", average);
      LOG_INFO_6ADDR(&dest_ipaddr);
      LOG_INFO_("\n");
    }
  }else{
    //not connected to root
    temperature = (float)get_temperature();
    LOG_INFO("Saving temperature reading: %f\n", temperature);

    readings[next_reading++] = temperature;
    if (next_reading == MAX_READINGS) {
        next_reading = 0;
    }
  }

  //with jitter reducing noise
  etimer_set(&periodic_timer, SEND_INTERVAL
      - CLOCK_SECOND + (random_rand() % (2 * CLOCK_SECOND)));
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
