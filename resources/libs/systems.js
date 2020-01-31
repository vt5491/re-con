// Note: you have to kill "lein.bat dev" and do a full re-compile in order to pick up on
// changes made to this script.
// import { System } from "./ecsy.module.js"; //no work
// import { System } from "./ecsy"; //no work
// import { System } from "./ecsy.js"; //no work
import {System} from 'ecsy/build/ecsy.js'; // works
import { Object3D, Position, Velocity, Rotating} from "./components.js";

console.log(`resource/libs/systems.js: entered`);
// MovableSystem
export class MovableSystemWrapper extends System {
   // This method will get called on every frame by default
   execute(delta, time) {
     re_con.test_scene_ecsy.movable_system_execute(this, delta, time);
     // Iterate through all the entities on the query
     // this.queries.moving.results.forEach(entity => {
     //   // var velocity = entity.getComponent(Velocity);
     //   var velocity = {x: 1.1, y: 1.1};
     //   var position = entity.getMutableComponent(Position);
     //   let obj = entity.getComponent(Object3D).object;
     //   re_pure_ecs_simple.main_scene.do_it(position);
     //   obj.position.x += velocity.x * delta;
     //   obj.position.y += velocity.y * delta;
     //
     // });
   }
 }
 // Define a query of entities that have "Velocity" and "Position" components
// MovableSystem.queries = {
//   moving: {
//     // components: [Velocity, Position]
//     components: [ Position]
//   }
// }

export class RotatingSystemWrapper extends System {
  execute(delta) {
    re_con.test_scene_ecsy.rotating_system_execute(this, delta);
    // let entities = this.queries.entities.results;
    // for (let i = 0; i < entities.length; i++) {
    //   let entity = entities[i];
    //   let rotatingSpeed = entity.getComponent(Rotating).rotatingSpeed;
    //   let object = entity.getComponent(Object3D).object;
    //
    //   object.rotation.x += rotatingSpeed * delta;
    //   object.rotation.y += rotatingSpeed * delta * 2;
    //   object.rotation.z += rotatingSpeed * delta * 3;
    // }
  }
}

// RotatingSystem.queries = {
//   entities: { components: [Rotating, Object3D] }
// };
