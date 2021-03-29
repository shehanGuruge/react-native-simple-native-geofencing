require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "RNSimpleNativeGeofencing"
  s.version      = "1.0.2"
  s.summary      = "RNSimpleNativeGeofencing"
  s.requires_arc = true
  s.homepage     = "https://github.com/shehanGuruge/react-native-simple-native-geofencing"
  s.license      = "MIT"
  s.author       = { "author" => "author@domain.cn" }
  s.platform     = :ios, "8.0"
  s.source       = { :git => "https://github.com/shehanGuruge/react-native-simple-native-geofencing.git"}
  s.source_files  = "ios/*.{h,m}"
  s.dependency 'React'
end

  
