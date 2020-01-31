export class Object3D {
  constructor() {
    this.object = null;
  }
}
var VT_STUFF = "hi2";
console.log(`VT_STUFF=${VT_STUFF}`);

// Position component
export class Position {
   constructor() {
     this.x = this.y = 0;
   }
 }

export class Velocity {
   constructor() {
     this.vx = this.vy = this.vz = 0;
   }
 }
 
 export class Rotating {
   constructor() {
     this.enabled = true;
     this.rotatingSpeed = 0;
     this.decreasingSpeed = 0.001;
   }
 }
