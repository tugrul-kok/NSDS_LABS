#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>
#include <string.h>
#include <math.h>

/*
 * Group number: 48
 *
 * Group members
 *  - Nisanur CAMUZCU
 *  - Tugrul KOK
 *  - Lukas GIRSCHICK
 */

const float min = 0;
const float max = 1000;
const float len = 1000;
const int num_ants = 8* 1000 * 1000;   //8* 1000 * 1000
const int num_food_sources = 10;      //10
const int num_iterations = 500;       //500

float random_position() {
  return (float) rand() / (float)(RAND_MAX/(max-min)) + min;
}

/*
 * Process 0 invokes this function to initialize food sources.
 */
void init_food_sources(float* food_sources) {
  for (int i=0; i<num_food_sources; i++) {
    food_sources[i] = random_position();
  }
}

/*
 * Process 0 invokes this function to initialize the position of ants.
 */
void init_ants(float* ants) {
  for (int i=0; i<num_ants; i++) {
    ants[i] = random_position();
  }
}

// Computes the average value
float compute_average(float *array, int num_elements)
{
  float sum = 0;
  for (int i = 0; i < num_elements; i++)
  {
    sum += array[i];
  }
  return ((float)sum) / num_elements;
}

// Computes the distance between ants and food sources and returns force
float dis_source(float pos, float* source)
{
  float distance = 1000;
  float force_distance = 0;
  for (int i = 0; i < num_food_sources; i++)
  {
    float curr_distance = abs(source[i] - pos);
    if(curr_distance < distance)
    {
      force_distance = pos - source[i];
      distance = curr_distance;
    }
  }
  return force_distance;
}

int main() {
  MPI_Init(NULL, NULL);
    
  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  srand(rank);


  // Allocate space in each process for food sources and ants
  // TODO
  const int  ants_per_proc = num_ants / num_procs;

  float *ants = (float*) malloc(ants_per_proc * sizeof(float));
  memset((void *) ants, 0, ants_per_proc * sizeof(float));

  float *source = (float*) malloc(num_food_sources * sizeof(float));
  
  // Process 0 initializes food sources and ants
  // TODO
  float *ini_ants = (float*) malloc(num_ants * sizeof(float));

  if (rank == 0)
  {
    init_ants(ini_ants);
    init_food_sources(source);
  }
  
  // Process 0 distributed food sources and ants
  // TODO
  
  MPI_Scatter(ini_ants, ants_per_proc, MPI_FLOAT, ants, ants_per_proc, MPI_FLOAT, 0, MPI_COMM_WORLD);
  
  MPI_Bcast(source,num_food_sources, MPI_FLOAT, 0, MPI_COMM_WORLD);
  
  free(ini_ants);


  // Iterative simulation
  float center = 0; // TODO
  for (int iter=0; iter<num_iterations; iter++) {
    // TODO
    
    //move ants to source or center
    for (int i = 0; i < ants_per_proc; i++)
    {
      float forece_center = (ants[i] - center) * 0.012;
      float force_food = dis_source(ants[i], source) * 0.01;

      ants[i] = forece_center + force_food + ants[i];
    }
    
    //local center of colony
    float local_center = compute_average(ants, ants_per_proc);
    MPI_Reduce(&local_center, &center, 1, MPI_FLOAT, MPI_SUM, 0, MPI_COMM_WORLD);

    if (rank == 0) {  
      //send actual center
      center = center / num_procs;
      printf("Iteration: %d - Average position: %f\n", iter, center);
    }

    MPI_Bcast(&center, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);
  }

  // Free memory
  // TODO
  free(ants);
  free(source);
  
  MPI_Barrier(MPI_COMM_WORLD);
  MPI_Finalize();
  return 0;
}
