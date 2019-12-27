
function setupControllers(vrHelper) {
  console.log("now in js setupControllers");
  vrHelper.onControllerMeshLoaded.add((webVRController)=>{
    console.log("callback level 1");
  });
};

function setupLoaders(scene) {
  console.log("now in setupLoaders");
  var assetsManager = new BABYLON.AssetsManager(scene);
  var textureTask = assetsManager.addTextureTask("image task", "imgs/burj_al_arab.jpg");
  textureTask.onSuccess = function(task) {
    console.log(`setupLoaders: texture loaded, task=${task}`);
    // material.diffuseTexture = task.texture;
  }

  assetsManager.load();
}
